package fr.hozakan.flysightcompanion.sessionmodule.business

import android.content.Context
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.dialogmodule.AwaitFlySightDeviceModeDialog
import fr.hozakan.flysightcompanion.dialogmodule.DialogResult
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.HudWarningDialog
import fr.hozakan.flysightcompanion.dialogmodule.OkDialogResult
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.MutableFlySightDevice
import fr.hozakan.flysightcompanion.locationmodule.LocationService
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.model.session.profile.SessionSource
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.ExitDetectorDelegate
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.FlareDetectorDelegate
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind.DefaultFlyBlindSessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.DefaultPlaneDisplaySessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc.DefaultPpcHudSessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.FileGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.FlySightGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.LocalGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.model.SessionControllerState
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max

class DefaultSessionControllerService(
    private val context: Context,
    private val fsDeviceService: FsDeviceService,
    private val audioService: AudioService,
    private val recordService: RecordService,
    private val appVersionService: AppVersionService,
    private val displayService: DisplayService,
    private val locationService: LocationService,
    private val userPrefService: UserPrefService,
    private val dialogService: DialogService
) : SessionControllerService {

    private val _state = MutableStateFlow<SessionControllerState>(SessionControllerState.Idle)
    override val state: StateFlow<SessionControllerState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob())

    private var job: Job? = null
    private var _sessionController = MutableStateFlow<SessionController?>(null)
    override val sessionController: StateFlow<SessionController?> = _sessionController.asStateFlow()

    override suspend fun playSession(
        sessionType: SessionType,
        sessionProfile: SessionProfile,
        sessionSource: SessionSource,
        flyBlindConfiguration: FlyBlindConfiguration?
    ) {
        val currentState = _state.value
        if (currentState is SessionControllerState.Playing) {
            // Already playing a session, handle accordingly
            return
        }
        job = scope.launch {
            val gnssSource = when (sessionSource) {
                SessionSource.Local -> LocalGnssSource(
                    locationService = locationService
                )

                is SessionSource.FlySight -> {
                    val fsDevice =
                        fsDeviceService.devices.value.firstOrNull { it.volatileUuid == sessionSource.fsId }
                            ?: return@launch
                    if (fsDevice.deviceMode.value != DeviceMode.Active) {
                        val result = dialogService.displayDialog(AwaitFlySightDeviceModeDialog {
                            fsDevice.deviceMode.first { it == DeviceMode.Active }
                        })
                        if (result != OkDialogResult) {
                            null
                        } else {
                            FlySightGnssSource(
                                fsDevice = fsDevice as MutableFlySightDevice
                            )
                        }
                    } else {
                        FlySightGnssSource(
                            fsDevice = fsDevice as MutableFlySightDevice
                        )
                    }
                }

                is SessionSource.Record -> {
                    FileGnssSource(
                        recordFile = sessionSource.file,
                        recordService = recordService
                    )
                }
            } ?: return@launch

            val shouldLaunch = if (sessionSource !is SessionSource.Record) {
                val acknowledged = dialogService.displayDialog(HudWarningDialog)
                when (acknowledged) {
                    DialogResult.Dismiss -> false
                    OkDialogResult -> true
                    else -> error("Unexpected dialog result: $acknowledged")
                }
            } else {
                true
            }

            if (!shouldLaunch) {
                _state.value = SessionControllerState.Idle
                return@launch
            }
            _state.value = SessionControllerState.Playing(sessionType, sessionProfile)

            val exitDetector = ExitDetectorDelegate(
                gnssFlow = gnssSource.gnssFlow,
                minExitDetectionAltMeter = sessionProfile.exitDetectionWindowBottom,
                maxExitDetectionAltMeter = sessionProfile.exitDetectionWindowTop,
                upThreshCmps = sessionProfile.exitUpThresh,
                downThreshCmps = sessionProfile.exitDownThresh,
                numDown = max(sessionProfile.exitPointsDown, 0),
                numUp = max(sessionProfile.exitPointsUp, 0),
                dzElevation = sessionProfile.configFile.dzElev
            )

            val controller = when (sessionType) {
                SessionType.Hud -> DefaultPpcHudSessionController(
                    context = context,
                    audioService = audioService,
                    displayService = displayService,
                    recordService = recordService,
                    appVersionService = appVersionService,
                    exitDetectorDelegate = exitDetector,
                    flareDetectorDelegate = FlareDetectorDelegate(
                        gnssFlow = gnssSource.gnssFlow,
                        exitDetectionFlow = exitDetector.exitFound
                    ),
                    gnssSource = gnssSource,
                    profile = sessionProfile,
                    type = sessionType,
                )

                SessionType.PlaneDisplay -> DefaultPlaneDisplaySessionController(
                    type = sessionType,
                    gnssSource = gnssSource,
                    userService = userPrefService
                )

                SessionType.FlyBlind -> DefaultFlyBlindSessionController(
                    context = context,
                    recordService = recordService,
                    appVersionService = appVersionService,
                    audioService = audioService,
                    configuration = flyBlindConfiguration!!,
                    gnssSource = gnssSource,
                    exitDetectorDelegate = exitDetector,
                    type = sessionType
                )

                SessionType.SpaceInvaders -> TODO()
                SessionType.FlyToDraw -> TODO()
            }

            _sessionController.value = controller
//            displayService.lockDisplay(true)
            _sessionController.value?.play(object : SessionController.SessionControllerCallback {
                override fun onDone() {
                    Timber.d("Hoz5 session done")
//                    displayService.lockDisplay(false)
//                    _state.value = SessionControllerState.Idle
//                    _sessionController.value = null
                }
            })
        }
    }

    override suspend fun stopSession() {
        job?.cancel()
        job = null
        _state.value = SessionControllerState.Idle
        _sessionController.value?.destroy()
        _sessionController.value = null
    }

    override fun resetExitDetection() {
        scope.launch {
            _sessionController.value?.resetDetectors()
        }
    }
}