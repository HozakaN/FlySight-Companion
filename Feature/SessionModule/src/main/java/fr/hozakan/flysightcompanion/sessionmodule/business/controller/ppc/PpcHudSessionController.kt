package fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc

import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.ExitDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.FlareDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PpcHudSessionController : SessionController, ExitDetector, FlareDetector {

    val sessionEvents: SharedFlow<SessionEvent>

    val profile: SessionProfile

    val performanceLanes: StateFlow<List<PpcHudVideoControllerImpl.PerformanceLine>>

    val laneStartPoint: StateFlow<GnssData?>

    val distanceToCenter: StateFlow<Float?>

    val referencePointDistances: StateFlow<Map<String, Double>>

    val timeInWindow: StateFlow<Float>
    val distanceInWindow: StateFlow<Int>
    val speedInWindow: StateFlow<Int>
}