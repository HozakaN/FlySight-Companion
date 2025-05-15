package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.FakeGnssData
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.recordsmodule.business.analyze.DefaultRecordParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZoneOffset
import kotlin.math.abs

class FileGnssSource(
    recordFile: RecordFile,
    recordService: RecordService
) : GnssSource {

    private val _timeMutableSource = TimeMutableSourceImpl()

    private val _gnssFlow = MutableSharedFlow<GnssData>()
    override val gnssFlow: SharedFlow<GnssData> = _gnssFlow.asSharedFlow()

    override val timeMutableSource: TimeMutableSource? = _timeMutableSource

    private val scope = CoroutineScope(SupervisorJob())

    init {
        scope.launch {
            val recordFileContent = recordService.loadRecordRawContent(recordFile)
            if (recordFileContent == null) return@launch
            val parser = DefaultRecordParser()
            val dataPoints = parser.parse(recordFileContent.lines())
            if (dataPoints.size > 1) {
                launch {
                    val firstDataPoint = dataPoints.first()
                    val lastDataPoint = dataPoints.last()
                    val firstDataPointTime =
                        firstDataPoint.dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
                    val timeDiffSeconds = (lastDataPoint.dateTime.toInstant(ZoneOffset.UTC)
                        .toEpochMilli() - firstDataPointTime) / 1_000f
                    _timeMutableSource.setStartValue(0f)
                    _timeMutableSource.setEndValue(timeDiffSeconds)

                    var hasEmittedDataEnd = false

                    while (isActive) {
                        if (_timeMutableSource.awaitUserInteractionEnd()) {
                            hasEmittedDataEnd = false
                        }
                        val currentTime = _timeMutableSource.currentTime.value
                        val currentPoint = dataPoints.minBy {
                            abs(
                                (it.dateTime.toInstant(ZoneOffset.UTC)
                                    .toEpochMilli() - firstDataPointTime) / 1_000f - currentTime
                            )
                        }
                        val dataToEmit = GnssData(
                            iTow = 0.toUInt(),
                            lon = currentPoint.longitude.toInt(),
                            lat = currentPoint.latitude.toInt(),
                            hMsl = currentPoint.hMSL.toInt(),
                            velN = currentPoint.velN.toInt(),
                            velE = currentPoint.velE.toInt(),
                            velD = currentPoint.velD.toInt(),
                            gpsFix = currentPoint.numSV,
                            vAcc = currentPoint.vAcc.toInt(),
                            speed = 0,
                            gSpeed = 0
                        )
                        val nextPoint = dataPoints.getOrNull(dataPoints.indexOf(currentPoint) + 1)
                        if (nextPoint != null) {
                            val timeDiff =
                                nextPoint.dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() -
                                        currentPoint.dateTime.toInstant(ZoneOffset.UTC)
                                            .toEpochMilli()
                            _gnssFlow.emit(value = dataToEmit)
                            if (!_timeMutableSource.awaitUserInteractionEnd()) {
                                _timeMutableSource.setCurrentTime(_timeMutableSource.currentTime.value + timeDiff / 1_000f)
                                delay(timeDiff)
                            }
                        } else {
                            if (!hasEmittedDataEnd) {
                                _gnssFlow.emit(FakeGnssData)
                                hasEmittedDataEnd = true
                            }
                        }
                    }
                }
            }
        }
    }
}