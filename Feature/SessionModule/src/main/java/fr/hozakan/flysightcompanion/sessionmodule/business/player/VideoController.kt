package fr.hozakan.flysightcompanion.sessionmodule.business.player

import android.content.Context
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.model.display.Display
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class VideoController(
    private val context: Context,
    private val sessionProfile: SessionProfile,
    private val displayService: DisplayService,
//    private val selectedDisplay: Display,
    private val sessionEvents: SharedFlow<SessionEvent>
) {

    private var presentation: VideoPresentation? = null
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("VideoController"))

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
        }
    }
}