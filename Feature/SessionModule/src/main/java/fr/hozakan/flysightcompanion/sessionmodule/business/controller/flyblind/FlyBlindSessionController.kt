package fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind

import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.ExitDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import kotlinx.coroutines.flow.StateFlow

interface FlyBlindSessionController : SessionController, ExitDetector {
    val configuration: FlyBlindConfiguration
    val referencePoint: ReferencePoint
    val elevation: StateFlow<Int>
    val distanceToRefPoint: StateFlow<Double>
    val headingToRefPoint: StateFlow<Double>

}