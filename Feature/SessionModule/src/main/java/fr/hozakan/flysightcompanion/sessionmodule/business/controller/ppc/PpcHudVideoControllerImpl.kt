package fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc

import android.content.Context
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.display.Display
import fr.hozakan.flysightcompanion.model.session.profile.Coordinate
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionEvent
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.VideoController
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PpcHudVideoControllerImpl(
    private val context: Context,
    private val sessionProfile: SessionProfile,
    private val displayService: DisplayService,
//    private val selectedDisplay: Display,
    private val sessionEvents: SharedFlow<SessionEvent>,
    private val exitDetection: StateFlow<GnssData?>,
    private val laneStartDetection: StateFlow<GnssData?>
) : VideoController {

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("VideoController"))

    // Data class to represent a line on the map
    data class PerformanceLine(
        val points: List<Coordinate>,
        val isReference: Boolean // true for center red line, false for green boundary lines
    )
//
//    // StateFlow to hold the three performance lines
//    private val _performanceLines = MutableStateFlow<List<PerformanceLine>>(emptyList())
//    override val performanceLines: StateFlow<List<PerformanceLine>> = _performanceLines.asStateFlow()

    // Cached values to avoid recalculating the lines unnecessarily
//    private var exitPoint: GnssData? = null
//    private var laneStartPoint: GnssData? = null

    private val _heightRepresentation = MutableStateFlow(0)
    val heightRepresentation = _heightRepresentation.asStateFlow()

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
//        exitDetection
//            .onEach { exitDetection ->
//                exitPoint = exitDetection
//                updatePerformanceLanes()
//            }
//            .launchIn(scope)
//        laneStartDetection
//            .onEach { laneStartDetection ->
//                laneStartPoint = laneStartDetection
//                updatePerformanceLanes()
//            }
//            .launchIn(scope)
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
            is SessionEvent.ExitFound -> {}
            is SessionEvent.PerformanceLaneStart -> {}
            SessionEvent.CompetitionWindowEntered -> {}
            SessionEvent.CompetitionWindowExited -> {}
        }
    }

    override fun destroy() {
        scope.cancel()
    }
//
//    override fun moveTo(gnssData: GnssData) {
//
//        // Use dateTime for time comparison instead of separate timestamp fields
//        val currentTime = gnssData.iTow
//        val exitTime = exitPoint?.iTow ?: run {
//            _performanceLines.value = emptyList()
//            return
//        }
//
//        if (currentTime - exitTime < 0.toUInt()) {
//            // Current time is before exit detection
//            exitPoint = null
//            _performanceLines.value = emptyList() // Clear lines
//        }
//
//        val laneStartTime = laneStartPoint?.iTow ?: run {
//            _performanceLines.value = emptyList()
//            return
//        }
//
//        if (currentTime - laneStartTime < 0.toUInt()) {
//            // Current time is before lane start
//            laneStartPoint = null
//            _performanceLines.value = emptyList() // Clear lines
//        }
//
//        // If we still have the necessary points, update the lines
//        if (exitPoint != null || laneStartPoint != null) {
//            updatePerformanceLanes()
//        }
//    }

}