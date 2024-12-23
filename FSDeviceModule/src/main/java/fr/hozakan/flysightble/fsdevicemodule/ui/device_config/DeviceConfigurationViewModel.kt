package fr.hozakan.flysightble.fsdevicemodule.ui.device_config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightble.userpreferencesmodule.UserPrefService
import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigEncoder
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeviceConfigurationViewModel @Inject constructor(
    private val userPrefService: UserPrefService
) : ViewModel() {

    private val encoder = DefaultConfigEncoder()

    private val _state = MutableStateFlow(
        DeviceConfigurationState(
            rawConfiguration = encoder.encodeConfig(defaultConfigFile()),
            configuration = defaultConfigFile(),
            unitSystem = userPrefService.unitSystem.value,
            showConfigAsRaw = false
        )
    )

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userPrefService.unitSystem
                .collect { unitSystem ->
                    _state.update {
                        it.copy(
                            unitSystem = unitSystem
                        )
                    }
                }
        }
        viewModelScope.launch {
            userPrefService.showConfigAsRaw
                .collect { showConfigAsRaw ->
                    _state.update {
                        it.copy(
                            showConfigAsRaw = showConfigAsRaw
                        )
                    }
                }
        }
    }

    fun loadConfiguration(conf: ConfigFile) {
        _state.update {
            it.copy(
                rawConfiguration = encoder.encodeConfig(conf),
                configuration = conf
            )
        }
    }

    fun updateShowConfigAsRaw(showConfigAsRaw: Boolean) {
        viewModelScope.launch {
            userPrefService.updateShowConfigAsRaw(showConfigAsRaw)
        }
    }

}