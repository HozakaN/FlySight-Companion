package fr.hozakan.flysightcompanion.sessionmodule.ui.prepare_session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.locationmodule.LocationAvailabilityState
import fr.hozakan.flysightcompanion.locationmodule.LocationService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionProfilesService
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSourceType
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionControllerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PrepareSessionViewModel @Inject constructor(
    fsDeviceService: FsDeviceService,
    recordService: RecordService,
    private val sessionProfilesService: SessionProfilesService,
    private val sessionControllerService: SessionControllerService,
    private val locationService: LocationService,
    private val permissionsService: AndroidPermissionsService
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            PrepareSessionState(
                prepareSessionPhase = PrepareSessionPhase.SelectProfile,
                sessionProfiles = LoadingState.Loading(),
                selectedProfile = null,
                selectedSourceType = SessionSourceType.Local,
                selectedSource = null,
                availableSources = emptyList(),
                doneEvent = null,
                locationAvailabilityState = LocationAvailabilityState.ForegroundLocationNotAllowed
            )
        )

    val state = _state.asStateFlow()

    init {
        sessionProfilesService.sessionProfiles
            .onEach {
                _state.update { aState ->
                    Timber.d("Hoz4 new session profiles: $it")
                    aState.copy(
                        sessionProfiles = LoadingState.Loaded(it),
                        selectedProfile = if (aState.selectedProfile != null) {
                            it.find { profile -> profile.name == aState.selectedProfile.name }
                        } else {
                            null
                        }
                    )
                }
            }
            .launchIn(viewModelScope)

        fsDeviceService.devices
            .combine(recordService.records) { devices, records ->
                Timber.d("devices: $devices")
                devices.map { device -> SessionSource.FlySight(device.volatileUuid, device.name) } +
                        records.map { record -> SessionSource.Record(record) }
            }
            .onEach { sources ->
                _state.update {
                    it.copy(
                        availableSources = sources
                    )
                }
            }
            .launchIn(viewModelScope)

        locationService.locationAvailabilityState
            .onEach { locationState ->
                _state.update {
                    it.copy(
                        locationAvailabilityState = locationState
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun deleteSessionProfile(sessionProfile: SessionProfile) {
        viewModelScope.launch {
            sessionProfilesService.deleteProfile(sessionProfile)
            if (sessionProfile == _state.value.selectedProfile) {
                _state.update {
                    it.copy(
                        selectedProfile = null
                    )
                }
            }
        }
    }

    fun duplicateSessionProfile(sessionProfile: SessionProfile) {
        viewModelScope.launch {
            sessionProfilesService.duplicateProfile(sessionProfile)
        }
    }

    fun onSessionProfileSelected(sessionProfile: SessionProfile) {
        _state.update {
            it.copy(
                selectedProfile = sessionProfile
            )
        }
    }

    fun onNextClicked() {
        if (_state.value.prepareSessionPhase == PrepareSessionPhase.SelectProfile && _state.value.selectedProfile != null) {
            _state.update {
                it.copy(
                    prepareSessionPhase = PrepareSessionPhase.SelectSource
                )
            }
        } else if (
            _state.value.prepareSessionPhase == PrepareSessionPhase.SelectSource &&
            _state.value.selectedSource != null ||
            (_state.value.selectedSourceType == SessionSourceType.Local &&
                    _state.value.locationAvailabilityState != LocationAvailabilityState.ForegroundLocationNotAllowed)
        ) {
            val profile = _state.value.selectedProfile ?: return
            val source = _state.value.selectedSource ?: SessionSource.Local
            viewModelScope.launch {
                sessionControllerService.playSession(
                    sessionProfile = profile,
                    sessionSource = source
                )
            }
//            _state.update {
//                it.copy(
//                    doneEvent = true.asEvent()
//                )
//            }
        }
    }

    fun onPrevClicked() {
        if (_state.value.prepareSessionPhase == PrepareSessionPhase.SelectSource) {
            _state.update {
                it.copy(
                    prepareSessionPhase = PrepareSessionPhase.SelectProfile
                )
            }
        }
    }

    fun onSourceTypeSelected(sourceType: SessionSourceType) {
        _state.update {
            it.copy(
                selectedSourceType = sourceType,
                selectedSource = null
            )
        }
    }

    fun onSourceSelected(source: SessionSource) {
        _state.update {
            it.copy(
                selectedSource = source
            )
        }
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            val granted = permissionsService.requestForegroundLocationPermission()
            updateLocationAvailabilityState()
//            if (granted) {
//                checkLocationSettings()
//            }
        }
    }

    fun checkLocationSettings() {
        viewModelScope.launch {
            val enabled = locationService.isLocationSettingsEnabled()
            if (!enabled) {
                locationService.ensureLocationSettingsEnabled()
            }
            updateLocationAvailabilityState()
        }
    }

    private suspend fun updateLocationAvailabilityState() {
        _state.update {
            it.copy(
                locationAvailabilityState = checkLocationAvailabilityState()
            )
        }
    }

    private suspend fun checkLocationAvailabilityState(): LocationAvailabilityState =
        if (permissionsService.hasForegroundLocationPermission()) {
            if (locationService.isLocationSettingsEnabled()) {
                LocationAvailabilityState.LocationAvailable
            } else {
                LocationAvailabilityState.SettingNotEnabled
            }
        } else {
            LocationAvailabilityState.ForegroundLocationNotAllowed
        }
}
