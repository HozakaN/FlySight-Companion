package fr.hozakan.flysightcompanion.sessionmodule.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionConfigurationsService
import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration
import fr.hozakan.flysightcompanion.model.session.configuration.StaticSessionSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionConfigViewModel @Inject constructor(
    private val sessionConfigurationsService: SessionConfigurationsService
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            SessionConfigState(
                sessionConfiguration = SessionConfiguration.default(),
                staticSessionSource = StaticSessionSource.FlySight
            )
        )

    val state = _state.asStateFlow()

    /**
     * Whether we are creating or editing a configuration
     */
    private var isCreatingConf = false

    fun loadSessionConfiguration(configurationName: String) {
        isCreatingConf = configurationName.isEmpty()
        if (configurationName.isEmpty()) {
            _state.update {
                it.copy(
                    sessionConfiguration = SessionConfiguration.default(),
                    configurationFound = true
                )
            }
        } else {
            val configFile =
                sessionConfigurationsService.sessionConfigurations.value.firstOrNull { it.name == configurationName }
            if (configFile != null) {
                _state.update {
                    it.copy(
                        sessionConfiguration = configFile,
                        configurationFound = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        configurationFound = false
                    )
                }
            }
        }
    }

    fun saveSessionConfiguration(sessionConfiguration: SessionConfiguration) {
        if (sessionConfiguration.name.isBlank()) {
            _state.update {
                it.copy(
                    fileSaved = false.asEvent()
                )
            }
        } else {
            viewModelScope.launch {
                val oldConf = _state.value.sessionConfiguration
                if (!isCreatingConf) {
                    sessionConfigurationsService.updateSessionConfiguration(
                        oldConf,
                        sessionConfiguration
                    )
                } else {
                    sessionConfigurationsService.saveConfigFile(sessionConfiguration)
                }
                isCreatingConf = false
                _state.update {
                    it.copy(
                        sessionConfiguration = sessionConfiguration,
                        fileSaved = true.asEvent()
                    )
                }
            }
        }
    }

}