package fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display

import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.FakeGnssData
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.FileGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.GnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.TimeMutableSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.VideoController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.BatteryLevel
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class DefaultPlaneDisplaySessionController(
    override val type: SessionType,
    private val gnssSource: GnssSource,
    private val userService: UserPrefService,
) : PlaneDisplaySessionController {

    private val _elevation = MutableStateFlow(0)
    override val elevation: StateFlow<Int> = _elevation.asStateFlow()

    override val dzElevation: StateFlow<Int> = userService.planeDisplayDzElev

    override val discipline: StateFlow<Int> = userService.planeDisplayDiscipline

    override val colorBlindOption: StateFlow<Boolean> = userService.planeDisplayColorBlindOption

    private var callback: SessionController.SessionControllerCallback? = null

    override val timeMutableSource: TimeMutableSource? = gnssSource.timeMutableSource

    override val deviceState: StateFlow<Pair<Pair<DeviceConnectionState, DeviceMode>, Pair<BatteryLevel, Boolean>>?> = gnssSource.deviceState
    override val gnssFlow: SharedFlow<GnssData> = gnssSource.gnssFlow

    override val videoController: VideoController = PlaneDisplayVideoController()

    private val scope =
        CoroutineScope(SupervisorJob() + CoroutineName("DefaultPlaneDisplaySessionController"))
    private var startJob: Job? = null

    private fun start() {
        startJob?.cancel()
        startJob = scope.launch {
            timeMutableSource?.let { source ->
                launch {
                    source.userInteractionEvent
                        .collect {
                            val pickedTiming = source.userInteractionEndEvent.first()
                            (gnssSource as? FileGnssSource)?.getGnssPointsUpToTime(pickedTiming.toLong() * 1_000L)
                                ?.let { pastGnssData ->
//                                    sessionComputationUnit.handleDataBatch(pastGnssData)
//                                    exitDetectorDelegate.clearAndProcessExitDetectionData(pastGnssData)
//                                    flareDetectorDelegate.clearAndProcessFlareDetectionData(pastGnssData)
                                    //FIXME May be an issue since performanceLane is moving when playing with the slider
//                                    pastGnssData.lastOrNull()?.let { data ->
//                                        moveTo(data)
//                                    }
                                }
                        }
                }
            }
            combine(userService.planeDisplayDzElev, gnssFlow) { dzElev, gnssData ->
                if (gnssData == FakeGnssData) {
                    callback?.onDone()
                } else {
                    eatData(dzElev, gnssData)
                }
            }
                .launchIn(scope)
        }
    }

    private fun eatData(dzElev: Int, gnssData: GnssData) {
        _elevation.value = gnssData.hMsl - dzElev
    }

    override fun pause() {
        startJob?.cancel()
        startJob = null
    }

    override fun play() {
        if (startJob == null) {
            start()
        }
    }

    override fun play(callback: SessionController.SessionControllerCallback) {
        if (startJob == null) {
            this.callback = callback
            play()
        }
    }

    override suspend fun resetDetectors() {}

    override fun destroy() {
        scope.cancel()
    }

    override fun updateDzElev(dzElev: Int) {
        userService.updatePlaneDisplayDzElev(dzElev)
    }

    override fun updateDiscipline(discipline: Int) {
        userService.updatePlaneDisplayDiscipline(discipline)
    }

    override fun updateColorBlindOption(enabled: Boolean) {
        userService.updatePlaneDisplayColorBlindOption(enabled)
    }

}