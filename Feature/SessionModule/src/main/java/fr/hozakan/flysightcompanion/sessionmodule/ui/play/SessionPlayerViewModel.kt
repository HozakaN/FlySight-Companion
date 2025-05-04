package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionControllerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class SessionPlayerViewModel @Inject constructor(
    private val sessionControllerService: SessionControllerService
) : ViewModel() {

    private val _state = MutableStateFlow(SessionPlayerState(player = null))

    val state = _state.asStateFlow()

    init {
        sessionControllerService.sessionController
            .onEach { player ->
                _state.update {
                    it.copy(
                        player = player
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}