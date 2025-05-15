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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    
    // Store all GNSS data points for batch processing
    private val _allGnssPoints = MutableStateFlow<List<GnssDataWithTimestamp>>(emptyList())
    val allGnssPoints: StateFlow<List<GnssDataWithTimestamp>> = _allGnssPoints.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob())
    private var firstDataPointTime: Long = 0

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
                    firstDataPointTime = firstDataPoint.dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
                    val timeDiffSeconds = (lastDataPoint.dateTime.toInstant(ZoneOffset.UTC)
                        .toEpochMilli() - firstDataPointTime) / 1_000f
                    _timeMutableSource.setStartValue(0f)
                    _timeMutableSource.setEndValue(timeDiffSeconds)
                    
                    // Prepare all GNSS data points with timestamps for batch processing
                    val gnssPointsWithTimestamps = dataPoints.map { dataPoint ->
                        val relativeTimeMilliseconds = dataPoint.dateTime.toInstant(ZoneOffset.UTC)
                            .toEpochMilli() - firstDataPointTime

                        // Calculate GPS Time of Week (iTOW) in milliseconds
                        // GPS week starts on Sunday at 00:00:00 UTC
                        val dayOfWeek = dataPoint.dateTime.dayOfWeek.value % 7 // 0-based (Sunday = 0)
                        val secondsInDay = dataPoint.dateTime.hour * 3600 +
                                dataPoint.dateTime.minute * 60 +
                                dataPoint.dateTime.second
                        val msInDay = secondsInDay * 1000 + dataPoint.dateTime.nano / 1_000_000
                        val iTow = (dayOfWeek * 24 * 3600 * 1000 + msInDay).toUInt()

                        val gnssData = GnssData(
                            iTow = iTow,
                            lon = dataPoint.longitude,
                            lat = dataPoint.latitude,
                            hMsl = dataPoint.hMSL.toInt(),
                            velN = dataPoint.velN.toInt(),
                            velE = dataPoint.velE.toInt(),
                            velD = dataPoint.velD.toInt(),
                            gpsFix = dataPoint.numSV,
                            vAcc = dataPoint.vAcc.toInt(),
                            speed = 0,
                            gSpeed = 0
                        )
                        
                        GnssDataWithTimestamp(gnssData, relativeTimeMilliseconds)
                    }
                    _allGnssPoints.value = gnssPointsWithTimestamps

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
                        
                        // Calculate GPS Time of Week (iTOW) in milliseconds
                        // GPS week starts on Sunday at 00:00:00 UTC
                        val dayOfWeek = currentPoint.dateTime.dayOfWeek.value % 7 // 0-based (Sunday = 0)
                        val secondsInDay = currentPoint.dateTime.hour * 3600 +
                                          currentPoint.dateTime.minute * 60 +
                                          currentPoint.dateTime.second
                        val msInDay = secondsInDay * 1000 + currentPoint.dateTime.nano / 1_000_000
                        val iTow = (dayOfWeek * 24 * 3600 * 1000 + msInDay).toUInt()

                        val dataToEmit = GnssData(
                            iTow = iTow,
                            lon = currentPoint.longitude,
                            lat = currentPoint.latitude,
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
    
    /**
     * Get all GNSS data points up to the specified time in milliseconds
     */
    fun getGnssPointsUpToTime(timeMilliseconds: Long): List<GnssData> {
        return _allGnssPoints.value
            .filter { it.timeMilliseconds <= timeMilliseconds }
            .map { it.gnssData }
    }
    
    data class GnssDataWithTimestamp(
        val gnssData: GnssData,
        val timeMilliseconds: Long
    )
}

