package fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc

import android.content.Context
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.framework.math.computeHeading
import fr.hozakan.flysightcompanion.framework.math.computeSignedDistanceToLine
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.FakeGnssData
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.profile.Coordinate
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.AudioController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.ExitDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.FileGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.FlareDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.GnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.MutableExitDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.MutableFlareDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionEvent
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.TimeMutableSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.VideoController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.BatteryLevel
import fr.hozakan.flysightcompanion.sessionmodule.business.recorder.Recorder
import fr.hozakan.flysightcompanion.sessionmodule.business.recorder.TrackCsvRecorder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin

class DefaultPpcHudSessionController(
    context: Context,
    audioService: AudioService,
    displayService: DisplayService,
    recordService: RecordService,
    appVersionService: AppVersionService,
    private val exitDetectorDelegate: MutableExitDetector,
    private val flareDetectorDelegate: MutableFlareDetector,
    private val gnssSource: GnssSource,
    override val profile: SessionProfile,
    override val type: SessionType
) : PpcHudSessionController,
    ExitDetector by exitDetectorDelegate,
    FlareDetector by flareDetectorDelegate {

    private var callback: SessionController.SessionControllerCallback? = null

    override val gnssFlow = gnssSource.gnssFlow

    private val sessionComputationUnit = PpcHudSessionComputationUnit(
        profile = profile,
        exitDetector = exitDetectorDelegate,
    )

    override val referencePointDistances: StateFlow<Map<String, Double>> =
        sessionComputationUnit.referencePointDistances

    override val timeInWindow: StateFlow<Float> = sessionComputationUnit.timeInWindow

    override val distanceInWindow: StateFlow<Int> = sessionComputationUnit.distanceInWindow

    override val speedInWindow: StateFlow<Int> = sessionComputationUnit.speedInWindow

    override val timeMutableSource: TimeMutableSource?
        get() = gnssSource.timeMutableSource

    override val deviceState: StateFlow<Triple<DeviceConnectionState, BatteryLevel, DeviceMode>?> = gnssSource.deviceState

    override val hasFix: StateFlow<Boolean> = gnssSource.hasFix

    private val scope =
        CoroutineScope(SupervisorJob() + CoroutineName("DefaultPpcHudSessionController"))
    private var startJob: Job? = null

    private var gnssPoints = emptyList<GnssData>()

    private val audioController = AudioController(
        profile,
        profile.configFile,
        audioService,
        sessionComputationUnit.sessionEvents
    )

    private val _videoController = PpcHudVideoControllerImpl(
        context = context,
        sessionProfile = profile,
        displayService = displayService,
//        selectedDisplay = profile.selectedDisplay,
        sessionEvents = sessionComputationUnit.sessionEvents,
        exitDetection = sessionComputationUnit.exitDetected,
        laneStartDetection = sessionComputationUnit.laneStartPoint
    )
    override val videoController: VideoController = _videoController

    private var recorder: Recorder? = if (gnssSource !is FileGnssSource) {
        TrackCsvRecorder(
            context = context,
            recordService = recordService,
            gnssSource = gnssSource,
            appVersionService = appVersionService
        )
    } else {
        null
    }

//    private var recorder: Recorder? = TrackCsvRecorder(
//        context = context,
//        recordService = recordService,
//        gnssSource = gnssSource,
//        appVersionService = appVersionService
//    )

    override val laneStartPoint: StateFlow<GnssData?> = sessionComputationUnit.laneStartPoint

    override val heading: StateFlow<Double> = sessionComputationUnit.heading

    override val sessionEvents: SharedFlow<SessionEvent> = sessionComputationUnit.sessionEvents

    // StateFlow to hold the three performance lanes
    private val _performanceLanes =
        MutableStateFlow<List<PpcHudVideoControllerImpl.PerformanceLine>>(emptyList())
    override val performanceLanes: StateFlow<List<PpcHudVideoControllerImpl.PerformanceLine>> =
        _performanceLanes.asStateFlow()

    // StateFlow to hold the distance from lanes (negative if closer to left, positive if closer to right)
    private val _distanceToCenter = MutableStateFlow<Float?>(null)
    override val distanceToCenter: StateFlow<Float?> = _distanceToCenter.asStateFlow()

    private fun start() {
        startJob = scope.launch {
            timeMutableSource?.let { source ->
                launch {
                    source.userInteractionEvent
                        .collect {
                            val pickedTiming = source.userInteractionEndEvent.first()
                            (gnssSource as? FileGnssSource)?.getGnssPointsUpToTime(pickedTiming.toLong() * 1_000L)
                                ?.let { pastGnssData ->
                                    sessionComputationUnit.clear()
                                    exitDetectorDelegate.clear()
                                    flareDetectorDelegate.clear()
                                    pastGnssData.lastOrNull()?.let { data ->
                                        sessionComputationUnit.handleNewData(data)
                                        exitDetectorDelegate.handleNewData(data)
                                        flareDetectorDelegate.handleNewData(data)
                                        moveTo(data)
                                    }
                                }
                        }
                }
            }
            combine(
                sessionComputationUnit.exitDetected,
                laneStartPoint
            ) { exitPoint, laneStartPoint ->
                if (exitPoint != null && laneStartPoint != null) {
                    updatePerformanceLanes()
                } else {
                    _performanceLanes.value = emptyList()
                }
            }
                .launchIn(scope)
            gnssFlow.collect { gnssData ->
                if (gnssData == FakeGnssData) {
                    callback?.onDone()
                } else {
                    eatData(gnssData)
                }
            }
        }
    }

    override fun pause() {
        startJob?.cancel()
        startJob = null
    }

    override fun play() {
        if (startJob == null) {
            recorder?.start()
            start()
        }
    }

    override fun play(callback: SessionController.SessionControllerCallback) {
        if (startJob == null) {
            this.callback = callback
            play()
        }
    }

    override suspend fun resetDetectors() {
        exitDetectorDelegate.clear()
        flareDetectorDelegate.clear()
        sessionComputationUnit.reset()
        _performanceLanes.value = emptyList()
    }

    override fun destroy() {
        recorder?.stop()
        scope.cancel()
    }

    private fun eatData(gnssData: GnssData) {
        gnssPoints += gnssData
        if (gnssPoints.size > 500) {
            //drop after 500 points
            gnssPoints = gnssPoints.take(500)
        }
        sessionComputationUnit.handleNewData(gnssData)
        exitDetectorDelegate.handleNewData(gnssData)
        flareDetectorDelegate.handleNewData(gnssData)
//        updatePerformanceLanes()
        updateFlyerDistanceToPerformanceLanes(gnssData)
    }

    private fun updatePerformanceLanes() {
        if (_performanceLanes.value.isNotEmpty()) {
            return
        }
//        if (!profile.showPerformanceLaneInMap) {
//            _performanceLanes.value = emptyList()
//            return
//        }

        // We need both the lane start point and the reference point to create the lines
        val startPoint = laneStartPoint.value
        val referencePoint = profile.referencePoint

        if (startPoint == null || referencePoint == null) return

        val startCoord = Coordinate(
            latitude = startPoint.lat,
            longitude = startPoint.lon
        )
        val referenceCoord = referencePoint.coords

        // Create the center line (red line from lane start to reference point)
        val centerLine = createLine(startCoord, referenceCoord)

        // Calculate the heading between points
        val heading = computeHeading(startCoord, referenceCoord)

        // Create the two side lines (green lines)
        val laneWidthMeters = profile.performanceLaneWidth.toDouble()
        val leftLine = createParallelLine(centerLine.points, heading, laneWidthMeters / 2)
        val rightLine = createParallelLine(centerLine.points, heading, -laneWidthMeters / 2)

        _performanceLanes.value = listOf(centerLine, leftLine, rightLine)
    }

    private fun createLine(
        start: Coordinate,
        end: Coordinate
    ): PpcHudVideoControllerImpl.PerformanceLine {
        return PpcHudVideoControllerImpl.PerformanceLine(
            points = listOf(start, end),
            isReference = true
        )
    }

    private fun moveTo(gnssData: GnssData) {
        val exitPoint = sessionComputationUnit.exitDetected.value
        // Use dateTime for time comparison instead of separate timestamp fields
        val currentTime = gnssData.iTow
        val exitTime = exitPoint?.iTow ?: run {
            _performanceLanes.value = emptyList()
            return
        }

        if (currentTime - exitTime < 0.toUInt()) {
            // Current time is before exit detection
//            exitPoint = null
            _performanceLanes.value = emptyList() // Clear lines
        }

        val laneStartPoint = sessionComputationUnit.laneStartPoint.value
        val laneStartTime = laneStartPoint?.iTow ?: run {
            _performanceLanes.value = emptyList()
            return
        }

        if (currentTime - laneStartTime < 0.toUInt()) {
            // Current time is before lane start
//            laneStartPoint = null
            _performanceLanes.value = emptyList() // Clear lines
        }

        updatePerformanceLanes()
        updateFlyerDistanceToPerformanceLanes(gnssData)
    }

    private fun updateFlyerDistanceToPerformanceLanes(gnssData: GnssData) {
        // Check if performance lanes are enabled and exist
        val performanceLanes = _performanceLanes.value

        if (performanceLanes.isEmpty()) {
            _distanceToCenter.value = null
            return
        }

        // Get the reference (center) line and side lines
        val centerLine = performanceLanes.firstOrNull { it.isReference }
        val sideLines = performanceLanes.filter { !it.isReference }

        // Ensure we have necessary lines
        if (centerLine == null || sideLines.size != 2) {
            _distanceToCenter.value = null
            return
        }

        // Convert gnssData to coordinate
        val currentPosition = Coordinate(
            latitude = gnssData.lat,
            longitude = gnssData.lon
        )

        // Calculate perpendicular distance from current position to center line
        val centerLineStart = centerLine.points.first()
        val centerLineEnd = centerLine.points.last()

        // Distance from point to line calculation (signed distance - negative is left, positive is right)
        val distance = computeSignedDistanceToLine(
            currentPosition,
            centerLineStart,
            centerLineEnd
        )

        // Normalize the distance to range from -1 (left side) to 1 (right side)
        // Half lane width corresponds to distance of 1
        val laneWidthMeters = profile.performanceLaneWidth
        val normalizedDistance = (distance * 2 / laneWidthMeters).coerceIn(-1f, 1f)

        _distanceToCenter.value = normalizedDistance
    }

    private fun createParallelLine(
        points: List<Coordinate>,
        heading: Double,
        distanceMeters: Double
    ): PpcHudVideoControllerImpl.PerformanceLine {
        // Calculate the offset perpendicular to the heading
        val perpendicular = heading + Math.PI / 2

        // Constants for Earth calculations (WGS84 semi-major axis in meters)
        val EARTH_RADIUS = 6378137.0

        // Calculate the new points with offset
        val offsetPoints = points.map { point ->
            // Convert distance to latitude and longitude offsets
            val latRad = Math.toRadians(point.latitude)

            // Calculate offsets (approximate formula for small distances)
            val dLat = (distanceMeters * cos(perpendicular)) / EARTH_RADIUS
            val dLng = (distanceMeters * sin(perpendicular)) / (EARTH_RADIUS * cos(latRad))

            // Apply offsets
            Coordinate(
                latitude = point.latitude + Math.toDegrees(dLat),
                longitude = point.longitude + Math.toDegrees(dLng)
            )
        }

        return PpcHudVideoControllerImpl.PerformanceLine(
            points = offsetPoints,
            isReference = false
        )
    }

}