package fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.model.config.Alarm
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.SilenceWindow
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.config.Volume
import fr.hozakan.flysightcompanion.model.defaultConfigFile
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
        configFile = defaultConfigFile(),
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
        if (configFileName.isEmpty()) {
            isCreatingConf = true
            _state.update {
                it.copy(
                    configFile = defaultConfigFile(),
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
                        configFile = configFile.copy(),
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

    fun updateConfigFileName(fileName: String) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    name = fileName
                ),
                hasValidFileName = fileName.isNotBlank()
            )
        }
    }

    fun updateConfigFileDescription(description: String) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    description = description
                )
            )
        }
    }

    fun updateConfigFileKind(kind: String) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    kind = kind
                )
            )
        }
    }

    fun updateDynamicModel(dynamicModel: DynamicModel) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    dynamicModel = dynamicModel
                )
            )
        }
    }

    fun updateSamplePeriod(samplePeriod: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    samplePeriod = samplePeriod ?: 0
                )
            )
        }
    }

    fun updateToneMode(toneMode: ToneMode) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    toneMode = toneMode
                )
            )
        }
    }

    fun updateToneMinimum(toneMinimum: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    toneMinimum = toneMinimum ?: 0
                )
            )
        }
    }

    fun updateToneMaximum(toneMaximum: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    toneMaximum = toneMaximum ?: 0
                )
            )
        }
    }

    fun updateToneLimitBehaviour(toneLimitBehaviour: ToneLimitBehaviour) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    toneLimitBehaviour = toneLimitBehaviour
                )
            )
        }
    }

    fun updateToneVolume(toneVolume: Volume) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    toneVolume = toneVolume
                )
            )
        }
    }

    fun updateRateMode(rateMode: RateMode) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    rateMode = rateMode
                )
            )
        }
    }

    fun updateRateMinimumValue(rateValue: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    rateMinimumValue = rateValue ?: 0
                )
            )
        }
    }

    fun updateRateMaximumValue(rateValue: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    rateMaximumValue = rateValue ?: 0
                )
            )
        }
    }

    fun updateRateMinimum(rate: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    rateMinimum = rate ?: 0
                )
            )
        }
    }

    fun updateRateMaximum(rate: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    rateMaximum = rate ?: 0
                )
            )
        }
    }

    fun updateFlatLineAtMinimumRate(value: Boolean) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    flatLineAtMinimumRate = value
                )
            )
        }
    }

    fun updateUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            userPrefService.updateUnitSystem(unitSystem)
        }
    }

    fun updateSpeechRate(speechRate: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    speechRate = speechRate ?: 0
                )
            )
        }
    }

    fun updateSpeechVolume(volume: Volume) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    speechVolume = volume
                )
            )
        }
    }

    fun addSpeech(speech: Speech) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    speeches = it.configFile.speeches + speech
                )
            )
        }
    }

    fun deleteSpeech(speech: Speech) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    speeches = it.configFile.speeches - speech
                )
            )
        }
    }

    fun updateVerticalThreshold(verticalThreshold: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    verticalThreshold = verticalThreshold ?: 0
                )
            )
        }
    }

    fun updateHorizontalThreshold(horizontalThreshold: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    horizontalThreshold = horizontalThreshold ?: 0
                )
            )
        }
    }

    fun updateUseSAS(useSAS: Boolean) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    useSAS = useSAS
                )
            )
        }
    }

    fun updateInitMode(initMode: InitMode) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    initMode = initMode
                )
            )
        }
    }

    fun updateInitFile(initFile: String?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    initFile = initFile
                )
            )
        }
    }

    fun updateAltitudeUnit(altitudeUnitSystem: UnitSystem) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    altitudeUnit = altitudeUnitSystem
                )
            )
        }
    }

    fun updateAltitudeStep(altitudeStep: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    altitudeStep = altitudeStep ?: 1
                )
            )
        }
    }

    fun updateWindowAbove(windowAbove: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    windowAbove = windowAbove ?: 0
                )
            )
        }
    }

    fun updateWindowBelow(windowBelow: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    windowBelow = windowBelow ?: 0
                )
            )
        }
    }

    fun updateDzElev(dzElev: Int?) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    dzElev = dzElev ?: 0
                )
            )
        }
    }

    fun addAlarm(alarm: Alarm) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    alarms = it.configFile.alarms + alarm
                )
            )
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    alarms = it.configFile.alarms - alarm
                )
            )
        }
    }

    fun addSilenceWindow(silenceWindow: SilenceWindow) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    silenceWindows = it.configFile.silenceWindows + silenceWindow
                )
            )
        }
    }

    fun deleteSilenceWindow(silenceWindow: SilenceWindow) {
        _state.update {
            it.copy(
                configFile = it.configFile.copy(
                    silenceWindows = it.configFile.silenceWindows - silenceWindow
                )
            )
        }
    }

    fun saveConfigFile() {
        if (_state.value.configFile.name.isBlank()) {
            _state.update {
                it.copy(
                    hasValidFileName = false,
                    fileSaved = false.asEvent()
                )
            }
        } else {
            viewModelScope.launch {
                val oldConf = _state.value.editedConfiguration
                if (oldConf != null) {
                    configFileService.updateConfigFile(oldConf,_state.value.configFile)
                } else {
                    configFileService.saveConfigFile(state.value.configFile)
                }
                _state.update {
                    it.copy(
                        hasValidFileName = true,
                        fileSaved = true.asEvent()
                    )
                }
            }
        }
    }

}