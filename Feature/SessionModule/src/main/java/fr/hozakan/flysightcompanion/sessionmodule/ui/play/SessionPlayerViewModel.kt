package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.lifecycle.ViewModel
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionPlayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SessionPlayerViewModel @Inject constructor(
    private val sessionPlayerService: SessionPlayerService
) : ViewModel() {

    private val _state = MutableStateFlow(SessionPlayerState(truc = ""))

    val state = _state.asStateFlow()

    init {
    }
}