package fr.hozakan.flysightcompanion.sessionmodule.business.player

import android.content.Context
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.display.Display
import fr.hozakan.flysightcompanion.model.session.configuration.Coordinate
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class VideoControllerImpl(
    private val context: Context,
    private val sessionProfile: SessionProfile,
    private val displayService: DisplayService,
//    private val selectedDisplay: Display,
    private val sessionEvents: SharedFlow<SessionEvent>
) : VideoController {

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("VideoController"))

    // Data class to represent a line on the map
    data class PerformanceLine(
        val points: List<Coordinate>,
        val isReference: Boolean // true for center red line, false for green boundary lines
    )

    // StateFlow to hold the three performance lines
    private val _performanceLines = MutableStateFlow<List<PerformanceLine>>(emptyList())
    override val performanceLines: StateFlow<List<PerformanceLine>> = _performanceLines.asStateFlow()

    // Cached values to avoid recalculating the lines unnecessarily
    private var exitPoint: GnssData? = null
    private var laneStartPoint: GnssData? = null

    init {
        sessionEvents
            .onEach { event ->
                handleEvent(event)
            }
            .launchIn(scope)
        displayService.displays
            .onEach { displays ->
                handleDisplays(displays)
            }
            .launchIn(scope)
    }

    private fun handleDisplays(displays: List<Display>) {
//        if (displays.isNotEmpty()) {
//            val firstDisplay = displays.first()
//            val pres = presentation
//            if (pres != null) {
//                if (pres.display != firstDisplay.internal) {
//                    pres.dismiss()
//                }
//            }
//            presentation = VideoPresentation(context, displays.first().internal)
//            presentation?.show()
//        }
    }

    private fun handleEvent(event: SessionEvent) {
        when (event) {
            is SessionEvent.AlarmEvent -> {

            }

            is SessionEvent.PlayFileEvent -> {}
            is SessionEvent.PlayTextEvent -> {}
            is SessionEvent.ExitFound -> {
                exitPoint = event.exit
                updatePerformanceLanes()
            }
            is SessionEvent.PerformanceLaneStart -> {
                laneStartPoint = event.gnssData
                updatePerformanceLanes()
            }
        }
    }

    override fun moveTo(gnssData: GnssData) {

        // Use dateTime for time comparison instead of separate timestamp fields
        val currentTime = gnssData.iTow
        val exitTime = exitPoint?.iTow ?: return

        if (currentTime - exitTime < 0.toUInt()) {
            // Current time is before exit detection
            exitPoint = null
            _performanceLines.value = emptyList() // Clear lines
        }

        val laneStartTime = laneStartPoint?.iTow ?: return
        if (currentTime - laneStartTime < 0.toUInt()) {
            // Current time is before lane start
            laneStartPoint = null
            _performanceLines.value = emptyList() // Clear lines
        }

        // If we still have the necessary points, update the lines
        if (exitPoint != null || laneStartPoint != null) {
            updatePerformanceLanes()
        }
    }

    private fun updatePerformanceLanes() {

        if (!sessionProfile.showPerformanceLaneInMap) {
            _performanceLines.value = emptyList()
            return
        }

        // We need both the lane start point and the reference point to create the lines
        val startPoint = laneStartPoint
        val referencePoint = sessionProfile.referencePoint

        Timber.d("Hoz3 updating performance lanes with startPoint: $startPoint, referencePoint: $referencePoint")
        if (startPoint == null || referencePoint == null) return

        val startCoord = Coordinate(
            latitude = startPoint.lat,
            longitude = startPoint.lon
        )
        val referenceCoord = referencePoint.coords

        // Create the center line (red line from lane start to reference point)
        val centerLine = createLine(startCoord, referenceCoord, true)

        // Calculate the heading between points
        val heading = calculateHeading(startCoord, referenceCoord)

        // Create the two side lines (green lines)
        val laneWidthMeters = sessionProfile.performanceLaneWidth.toDouble()
        val leftLine = createParallelLine(centerLine.points, heading, laneWidthMeters / 2, false)
        val rightLine = createParallelLine(centerLine.points, heading, -laneWidthMeters / 2, false)

        _performanceLines.value = listOf(centerLine, leftLine, rightLine)
    }

    private fun createLine(start: Coordinate, end: Coordinate, isReference: Boolean): PerformanceLine {
        // For a simple line, we just use the start and end points
        return PerformanceLine(
            points = listOf(start, end),
            isReference = isReference
        )
    }

    private fun calculateHeading(from: Coordinate, to: Coordinate): Double {
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val fromLat = Math.toRadians(from.latitude)
        val toLat = Math.toRadians(to.latitude)

        val y = sin(dLng) * cos(toLat)
        val x = cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng)
        return (atan2(y, x) + 2 * Math.PI) % (2 * Math.PI) // Normalize to [0, 2π)
    }

    private fun createParallelLine(points: List<Coordinate>, heading: Double, distanceMeters: Double, isReference: Boolean): PerformanceLine {
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