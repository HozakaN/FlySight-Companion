package fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.defaultConfigFile
import fr.hozakan.flysightcompanion.model.emptyConfigFile
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfigDetailViewModel @Inject constructor(
    private val configFileService: ConfigFileService,
    private val userPrefService: UserPrefService
) : ViewModel() {

    private val _state = MutableStateFlow(ConfigDetailState(
        editedConfiguration = emptyConfigFile(),
        unitSystem = UnitSystem.Metric
    ))

    val state = _state.asStateFlow()

    /**
     * Whether we are creating or editing a configuration
     */
    private var isCreatingConf = false

    init {
        viewModelScope.launch {
            userPrefService.unitSystem.collect { unitSystem ->
                _state.update {
                    it.copy(
                        unitSystem = unitSystem
                    )
                }
            }
        }
    }

    fun loadConfigFile(configFileName: String) {
        isCreatingConf = configFileName.isEmpty()
        if (configFileName.isEmpty()) {
            _state.update {
                it.copy(
                    editedConfiguration = emptyConfigFile(),
                    configFileFound = true
                )
            }
        } else {
            val configFile =
                configFileService.configFiles.value.firstOrNull { it.name == configFileName }
            if (configFile != null) {
                _state.update {
                    it.copy(
                        editedConfiguration = configFile,
                        configFileFound = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        configFileFound = false
                    )
                }
            }
        }
    }

    fun updateUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            userPrefService.updateUnitSystem(unitSystem)
        }
    }

    fun saveConfigFile(configFile: ConfigFile) {
        if (configFile.name.isBlank()) {
            _state.update {
                it.copy(
                    fileSaved = false.asEvent()
                )
            }
        } else {
            viewModelScope.launch {
                val oldConf = _state.value.editedConfiguration
                if (!isCreatingConf) {
                    configFileService.updateConfigFile(oldConf,configFile)
                } else {
                    configFileService.saveConfigFile(configFile)
                }
                isCreatingConf = false
                _state.update {
                    it.copy(
                        editedConfiguration = configFile,
                        fileSaved = true.asEvent()
                    )
                }
            }
        }
    }

}