package fr.hozakan.flysightcompanion.sessionmodule.ui.pick_config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionConfigurationsService
import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class PickConfigViewModel @Inject constructor(
    private val sessionConfigurationsService: SessionConfigurationsService
) : ViewModel() {

    private val _state =
        MutableStateFlow(PickConfigState(sessionConfigurations = LoadingState.Loading()))

    val state = _state.asStateFlow()

    init {
        sessionConfigurationsService.sessionConfigurations
            .onEach {
                _state.update { aState ->
                    aState.copy(
                        sessionConfigurations = LoadingState.Loaded(it)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun deleteSessionConfiguration(sessionConfiguration: SessionConfiguration) {
        viewModelScope.launch {
            sessionConfigurationsService.deleteSessionConfiguration(sessionConfiguration)
        }
    }

    fun duplicateSessionConfiguration(sessionConfiguration: SessionConfiguration) {
        viewModelScope.launch {
            sessionConfigurationsService.duplicateSessionConfiguration(sessionConfiguration)
        }
    }

    fun onSessionConfigurationSelected(sessionConfiguration: SessionConfiguration) {

    }

}