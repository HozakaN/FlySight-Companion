package fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.VideoController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.ExitDetector
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("DefaultLocale")
class FlyBlindVideoController(
    exitDetector: ExitDetector,
    computationUnit: FlyBlindComputationUnit,
    private val configuration: FlyBlindConfiguration
) : VideoController {

    private val _distance = MutableStateFlow("-- NM")
    val distance = _distance.asStateFlow()

    private val _headingStr = MutableStateFlow("-- deg")
    private val _heading = MutableStateFlow(0.0)
    val headingStr = _headingStr.asStateFlow()
    val heading = _heading.asStateFlow()

    val displayMap = mutableStateOf(true)

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("FlyBlindVideoController") + Dispatchers.Default)

    init {
        exitDetector.exitFound
            .onEach {
                displayMap.value = it == null || configuration.keepMapOnExit
            }
            .launchIn(scope)
        computationUnit.distance
            .onEach { distanceDouble ->
                _distance.value = String.format("%.2f NM", distanceDouble)
            }
            .launchIn(scope)
        computationUnit.heading
            .onEach { headingDouble ->
                _heading.value = headingDouble
                _headingStr.value = String.format("%.2f deg", headingDouble)
            }
            .launchIn(scope)
    }

    override fun destroy() {
        scope.cancel()
    }

}
