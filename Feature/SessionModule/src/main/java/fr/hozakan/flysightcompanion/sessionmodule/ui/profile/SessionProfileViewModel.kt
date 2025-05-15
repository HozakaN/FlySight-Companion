package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionProfilesService
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.sessionmodule.business.ReferencePointsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionProfileViewModel @Inject constructor(
    private val sessionProfilesService: SessionProfilesService,
    configFileService: ConfigFileService,
    referencePointsService: ReferencePointsService
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            SessionProfileState(
                sessionProfile = SessionProfile.default(),
                configFiles = emptyList()
            )
        )

    val state = _state.asStateFlow()

    /**
     * Whether we are creating or editing a configuration
     */
    private var isCreatingConf = false

    init {
        configFileService.configFiles
            .onEach { configFiles ->
                _state.update {
                    it.copy(
                        configFiles = configFiles
                    )
                }
            }
            .launchIn(viewModelScope)
            
        // Load reference points
        referencePointsService.referencePoints
            .onEach { referencePoints ->
                _state.update {
                    it.copy(
                        referencePoints = referencePoints
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadSessionConfiguration(configurationName: String) {
        isCreatingConf = configurationName.isEmpty()
        if (configurationName.isEmpty()) {
            _state.update {
                it.copy(
                    sessionProfile = SessionProfile.default(),
                    configurationFound = true
                )
            }
        } else {
            val configFile =
                sessionProfilesService.sessionProfiles.value.firstOrNull { it.name == configurationName }
            if (configFile != null) {
                _state.update {
                    it.copy(
                        sessionProfile = configFile,
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

    fun saveSessionConfiguration(sessionProfile: SessionProfile) {
        if (sessionProfile.name.isBlank()) {
            _state.update {
                it.copy(
                    fileSaved = false.asEvent()
                )
            }
        } else {
            viewModelScope.launch {
                val oldConf = _state.value.sessionProfile
                if (!isCreatingConf) {
                    sessionProfilesService.updateProfile(
                        oldConf,
                        sessionProfile
                    )
                } else {
                    sessionProfilesService.saveProfile(sessionProfile)
                }
                isCreatingConf = false
                _state.update {
                    it.copy(
                        sessionProfile = sessionProfile,
                        fileSaved = true.asEvent()
                    )
                }
            }
        }
    }

}
