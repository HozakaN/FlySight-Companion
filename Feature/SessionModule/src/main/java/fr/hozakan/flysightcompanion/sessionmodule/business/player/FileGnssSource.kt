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
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZoneOffset

class FileGnssSource(
    recordFile: RecordFile,
    recordService: RecordService
) : GnssSource {

    private val _gnssFlow = MutableSharedFlow<GnssData>()
    override val gnssFlow: SharedFlow<GnssData> = _gnssFlow.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob())

    init {
        scope.launch {
            val recordFileContent = recordService.loadRecordRawContent(recordFile)
            if (recordFileContent == null) return@launch
            val parser = DefaultRecordParser()
            val dataPoints = parser.parse(recordFileContent.lines())
            if (dataPoints.size > 1) {
                launch {
                    dataPoints.forEachIndexed { index, point ->
                        if (index > 0) {
                            val previous = dataPoints[index - 1]
                            val previousGnssData = GnssData(
                                iTow = 0.toUInt(),
                                lon = previous.longitude.toInt(),
                                lat = previous.latitude.toInt(),
                                hMsl = previous.hMSL.toInt(),
                                velN = previous.velN.toInt(),
                                velE = previous.velE.toInt(),
                                velD = previous.velD.toInt(),
                                gpsFix = previous.numSV,
                                vAcc = previous.vAcc.toInt(),
                                gSpeed = 0
                            )
                            val timeDiff = point.dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() -
                                    previous.dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
                            Timber.d("Hoz5 emitting gnss data ${previous.dateTime.formatTime()}, ${previous.latitude}, ${previous.longitude}")
                            _gnssFlow.emit(value = previousGnssData)
                            delay(timeDiff)
                        }
                    }
                    Timber.d("Hoz5 emitting finished")
                    _gnssFlow.emit(FakeGnssData)
                }
            }
        }
    }
}