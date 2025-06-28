package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.coroutine.flow.asFlowEvent
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionProfilesService
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
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
    private val displayService: DisplayService,
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

    fun loadSessionConfiguration(profileName: String) {
        isCreatingConf = profileName.isEmpty()
        if (profileName.isEmpty()) {
            _state.update {
                val default = SessionProfile.default()
                it.copy(
                    sessionProfile = default.copy(
                        configFile = default.configFile.copy(name = "")
                    ),
                    profileFound = true
                )
            }
        } else {
            val sessionProfile =
                sessionProfilesService.sessionProfiles.value.firstOrNull { it.name == profileName }
            if (sessionProfile != null) {
                _state.update {
                    it.copy(
                        sessionProfile = sessionProfile,
                        profileFound = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        profileFound = false
                    )
                }
            }
        }
    }

    fun saveSessionConfiguration(sessionProfile: SessionProfile) {
        if (sessionProfile.name.isBlank()) {
            _state.update {
                it.copy(
                    fileSaved = false.asFlowEvent()
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
                        fileSaved = true.asFlowEvent()
                    )
                }
            }
        }
    }

    fun lockDisplay(lock: Boolean) {
        viewModelScope.launch {
            displayService.lockDisplay(lock = lock)
        }
    }

}
