package fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind

import android.content.Context
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.FakeGnssData
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.VideoController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.ExitDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.MutableExitDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.BatteryLevel
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.FileGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.GnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.TimeMutableSource
import fr.hozakan.flysightcompanion.sessionmodule.business.recorder.Recorder
import fr.hozakan.flysightcompanion.sessionmodule.business.recorder.TrackCsvRecorder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DefaultFlyBlindSessionController(
    context: Context,
    recordService: RecordService,
    appVersionService: AppVersionService,
    audioService: AudioService,
    override val configuration: FlyBlindConfiguration,
    private val gnssSource: GnssSource,
    private val exitDetectorDelegate: MutableExitDetector,
    override val type: SessionType
) : FlyBlindSessionController, ExitDetector by exitDetectorDelegate {

    private var callback: SessionController.SessionControllerCallback? = null

    private val _elevation = MutableStateFlow(0)
    override val referencePoint: ReferencePoint = configuration.referencePoint
    override val elevation: StateFlow<Int> = _elevation.asStateFlow()

    private val computationUnit = FlyBlindComputationUnit(configuration)

    override val distanceToRefPoint: StateFlow<Double> = computationUnit.distance

    override val headingToRefPoint: StateFlow<Double> = computationUnit.heading

    override val gnssFlow: SharedFlow<GnssData> = gnssSource.gnssFlow

    override val timeMutableSource: TimeMutableSource? = gnssSource.timeMutableSource

    override val deviceState: StateFlow<Pair<DeviceConnectionState, BatteryLevel>?> = gnssSource.deviceState

    private var recorder: Recorder? = if (gnssSource !is FileGnssSource) {
        TrackCsvRecorder(
            context = context,
            recordService = recordService,
            gnssSource = gnssSource,
            appVersionService = appVersionService
        )
    } else {
        null
    }

    private val audioController = FlyBlindAudioController(
        configuration = configuration,
        exitDetector = exitDetectorDelegate,
        heading = computationUnit.heading,
        audioService = audioService
    )

    private val scope =
        CoroutineScope(SupervisorJob() + CoroutineName("DefaultPlaneDisplaySessionController") + Dispatchers.Default)

    override val videoController: VideoController =
        FlyBlindVideoController(exitDetectorDelegate, computationUnit, configuration)
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
                                    exitDetectorDelegate.clear()
                                    computationUnit.reset()
                                    pastGnssData.forEach { data ->
                                        computationUnit.handleNewData(data)
                                        exitDetectorDelegate.handleNewData(data)
                                    }
                                }
                        }
                }
            }
            gnssFlow.collect { data ->
                if (data == FakeGnssData) {
                    callback?.onDone()
                } else {
                    eatData(data)
                }
            }
        }
    }

    override fun pause() {
        audioController.pause()
        startJob?.cancel()
        startJob = null
    }

    override fun play() {
        if (startJob == null) {
            recorder?.start()
            audioController.resume()
            start()
        }
    }

    override fun play(callback: SessionController.SessionControllerCallback) {
        if (startJob == null) {
            this.callback = callback
            play()
        }
    }

    override suspend fun resetDetectors() {
        exitDetectorDelegate.clear()
        computationUnit.reset()
    }

    override fun destroy() {
        recorder?.stop()
        videoController.destroy()
        audioController.destroy()
        scope.cancel()
    }

    private fun eatData(gnssData: GnssData) {
        computationUnit.handleNewData(gnssData)
    }
}