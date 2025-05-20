package fr.hozakan.flysightcompanion.sessionmodule.business.player

import android.content.Context
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.framework.math.computeSignedDistanceToLine
import fr.hozakan.flysightcompanion.framework.math.computeHeading
import fr.hozakan.flysightcompanion.model.FakeGnssData
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.configuration.Coordinate
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.sessionmodule.business.player.VideoControllerImpl.PerformanceLine
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

class DefaultSessionController(
    context: Context,
    audioService: AudioService,
    displayService: DisplayService,
    private val gnssSource: GnssSource,
    override val profile: SessionProfile
) : SessionController {

    private var callback: SessionController.SessionControllerCallback? = null

    override val gnssFlow = gnssSource.gnssFlow

    private val sessionComputationUnit = SessionComputationUnit(profile = profile)

    override val referencePointDistances: StateFlow<Map<String, Double>> = sessionComputationUnit.referencePointDistances

    override val timeMutableSource: TimeMutableSource?
        get() = gnssSource.timeMutableSource

    private val scope = CoroutineScope(SupervisorJob())
    private var startJob: Job? = null

    private var gnssPoints = emptyList<GnssData>()

    private val audioController = AudioController(
        profile,
        profile.configFile,
        audioService,
        sessionComputationUnit.sessionEvents
    )

    private val _videoController = VideoControllerImpl(
        context = context,
        sessionProfile = profile,
        displayService = displayService,
//        selectedDisplay = profile.selectedDisplay,
        sessionEvents = sessionComputationUnit.sessionEvents,
        exitDetection = sessionComputationUnit.exitDetected,
        laneStartDetection = sessionComputationUnit.laneStartPoint
    )
    override val videoController: VideoController = _videoController

    override val exitDetected: StateFlow<GnssData?> = sessionComputationUnit.exitDetected

    override val laneStartPoint: StateFlow<GnssData?> = sessionComputationUnit.laneStartPoint

    override val sessionEvents: SharedFlow<SessionEvent> = sessionComputationUnit.sessionEvents

    // StateFlow to hold the three performance lanes
    private val _performanceLanes = MutableStateFlow<List<PerformanceLine>>(emptyList())
    override val performanceLanes: StateFlow<List<PerformanceLine>> =
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
                                    sessionComputationUnit.handleDataBatch(pastGnssData)
                                    pastGnssData.lastOrNull()?.let { data ->
                                        moveTo(data)
                                    }
                                }
                        }
                }
            }
            combine(exitDetected, laneStartPoint) { exitPoint, laneStartPoint ->
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
            start()
        }
    }

    override fun play(callback: SessionController.SessionControllerCallback) {
        if (startJob == null) {
            this.callback = callback
            play()
        }
    }

    override fun resetExitDetection() {
        sessionComputationUnit.resetExitDetection()
    }

    override fun destroy() {
        scope.cancel()
    }

    private fun eatData(gnssData: GnssData) {
        gnssPoints += gnssData
        if (gnssPoints.size > 500) {
            //drop after 500 points
            gnssPoints = gnssPoints.take(500)
        }
        sessionComputationUnit.handleNewData(gnssData)
//        updatePerformanceLanes()
        updateFlyerDistanceToPerformanceLanes(gnssData)
    }

    private fun updatePerformanceLanes() {
        Timber.d("Hoz3 updatePerformanceLanes ${_performanceLanes.value.size}, laneStartPoint = ${laneStartPoint.value}")
        if (_performanceLanes.value.isNotEmpty()) return
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
        val centerLine = createLine(startCoord, referenceCoord, true)

        // Calculate the heading between points
        val heading = computeHeading(startCoord, referenceCoord)

        // Create the two side lines (green lines)
        val laneWidthMeters = profile.performanceLaneWidth.toDouble()
        val leftLine = createParallelLine(centerLine.points, heading, laneWidthMeters / 2, false)
        val rightLine = createParallelLine(centerLine.points, heading, -laneWidthMeters / 2, false)

        _performanceLanes.value = listOf(centerLine, leftLine, rightLine)
    }

    private fun createLine(
        start: Coordinate,
        end: Coordinate,
        isReference: Boolean
    ): PerformanceLine {
        // For a simple line, we just use the start and end points
        return PerformanceLine(
            points = listOf(start, end),
            isReference = isReference
        )
    }

    private fun moveTo(gnssData: GnssData) {
        val exitPoint = exitDetected.value
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
        distanceMeters: Double,
        isReference: Boolean
    ): PerformanceLine {
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

        return PerformanceLine(
            points = offsetPoints,
            isReference = isReference
        )
    }

}
