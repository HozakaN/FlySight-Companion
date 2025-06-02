package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.Direction
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber


class ExitDetectorDelegate(
    gnssFlow: SharedFlow<GnssData>,
    /**
     * Do not detect exits if lower than this altitude in mm above ground. Exit
     * detection is disabled if this is less than 0
     */
    private val minAltAglMeter: Int = -1,
    /**
     * Do not detect exit altitude until lower than this threshold. This check
     * is disabled if < 0.
     */
    private val cfgExitAltAglMeter: Int = -1,
    /**
     * Velocity_down (down is positive) to determine if going up (cm/s)
     */
    private val upThreshCmps: Int = -800,

    /** velocity_down (down is positive) to determine if going down (cm/s) */
    val downThreshCmps: Int = 800,

    /** Number of consecutive down measurements needed to set exit_alt_valid */
    val numDown: Int = 5,

    /** Number of consecutive up measurements needed to reset exit_alt_valid */
    val numUp: Int = 50,
    private val dzElevation: Int
) : MutableExitDetector {

    private val _exitFound: MutableStateFlow<GnssData?> = MutableStateFlow(null)
    override val exitFound: StateFlow<GnssData?> = _exitFound.asStateFlow()

    val isEnabled: Boolean = minAltAglMeter >= 0

    /** Is the exit altitude valid? */
    private var exitAltValid: Boolean = false

    /** Direction of previous `count` velocity measurements */
    private var direction: Direction = Direction.UP

    /** Number of velocity measurements */
    private var count: Int = 0

    /** The current exit altitude (MSL) in mm, latched into exit_alt_mm when
     * exit_alt_valid changes from false to true
     *
     * The current time of the exit in milliseconds since start of day,
     * latched into exit_alt_mm when exit_alt_valid changes from false to true
     * */
    private var currGnssData: GnssData? = null

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("ExitDetectorDelegate") + Dispatchers.Default)

    init {
        gnssFlow
            .onEach {
                handleNewData(it)
            }
            .launchIn(scope)
    }
    
    /**
     * Reset the detector to initial state
     */
    private fun reset() {
        exitAltValid = false
        direction = Direction.UP
        count = 0
        currGnssData = null
        _exitFound.value = null
    }

    override suspend fun clearAndProcessExitDetectionData(gnssDataList: List<GnssData>) {
        // Reset detector state
        reset()
        
        // Process all points up to the rewind index
        gnssDataList
            .forEach { gnssData ->
                handleNewData(gnssData)
            }
    }

    private fun handleNewData(
        gnssData: GnssData
    ) {
        if (minAltAglMeter < 0 || gnssData.hMsl < minAltAglMeter + dzElevation) {
            return
        }

        if (cfgExitAltAglMeter >= 0 && gnssData.hMsl > cfgExitAltAglMeter + dzElevation) {
            return
        }

        when (direction) {
            Direction.UP -> {
                if (gnssData.velD * 100 > downThreshCmps) {
                    count = 1
                    currGnssData = gnssData
                    direction = Direction.DOWN
                } else if (gnssData.velD * 100 < upThreshCmps) {
                    count++
                } else {
                    count = 0
                }
                if (exitAltValid && count > numUp) {
                    exitAltValid = false
                    _exitFound.value = null
                }
            }
            Direction.DOWN -> {
                if (gnssData.velD * 100 > downThreshCmps) {
                    if (count == 0) {
                        currGnssData = gnssData
                    }
                    count++
                } else if (gnssData.velD * 100 < upThreshCmps) {
                    count = 1
                    direction = Direction.UP
                } else {
                    count = 0
                }
                if (!exitAltValid && count > numDown) {
                    exitAltValid = true
                    _exitFound.value = currGnssData
                }
            }
        }
    }

}
