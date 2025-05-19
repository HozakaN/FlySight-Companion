package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionControllerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SessionPlayerViewModel @Inject constructor(
    private val sessionControllerService: SessionControllerService,
    private val displayService: DisplayService
) : ViewModel() {

    private val _state = MutableStateFlow(SessionPlayerState(controller = null))

    val state = _state.asStateFlow()

    init {
        sessionControllerService.sessionController
            .onEach { controller ->
                _state.update {
                    it.copy(
                        controller = controller
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun lockDisplay() {
        viewModelScope.launch {
            displayService.lockDisplay(true)
        }
    }

    fun onExitClicked() {
        viewModelScope.launch {
            sessionControllerService.stopSession()
            displayService.lockDisplay(false)
        }
    }

    fun resetExitDetection() {
        viewModelScope.launch {
            sessionControllerService.resetExitDetection()
        }
    }
}