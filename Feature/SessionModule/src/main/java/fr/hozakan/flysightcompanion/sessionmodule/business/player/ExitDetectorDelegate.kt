package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ExitDetectorDelegate(
    /**
     * Do not detect exits if lower than this altitude in mm above ground. Exit
     * detection is disabled if this is less than 0
     */
    private val minAltAglMm: Int = -1,
    /**
     * Do not detect exit altitude until lower than this threshold. This check
     * is disabled if < 0.
     */
    private val cfgExitAltAglMm: Int = -1,
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
) {

    private val _exitFound: MutableStateFlow<GnssData?> = MutableStateFlow(null)
    val exitFound: StateFlow<GnssData?> = _exitFound.asStateFlow()

    val isEnabled: Boolean = minAltAglMm >= 0

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

    fun handleNewData(
        gnssData: GnssData
    ) {
        if (minAltAglMm < 0 || gnssData.hMsl < minAltAglMm + dzElevation) {
            return
        }

        if (cfgExitAltAglMm >= 0 && gnssData.hMsl > cfgExitAltAglMm + dzElevation) {
            return
        }

        when (direction) {
            Direction.UP -> {
                if (gnssData.velD > downThreshCmps) {
                    count = 1
                    currGnssData = gnssData
                    direction = Direction.DOWN
                } else if (gnssData.velD < upThreshCmps) {
                    count++
                } else {
                    count = 0
                }
                if (exitAltValid && count > numUp) {
                    exitAltValid = false
                }
            }
            Direction.DOWN -> {
                if (gnssData.velD > downThreshCmps) {
                    if (count == 0) {
                        currGnssData = gnssData
                    }
                    count++
                } else if (gnssData.velD < upThreshCmps) {
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

    enum class Direction {
        UP,
        DOWN
    }
}