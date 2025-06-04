package fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector

import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.Direction
import fr.hozakan.flysightcompanion.model.session.Flare
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class FlareDetectorDelegate(
    private val gnssFlow: SharedFlow<GnssData>,
    private val exitDetectionFlow: StateFlow<GnssData?>,
    /**
     * Distance to travel up to detect a flare, in meters
     */
    private val distanceUp: Int = 5
) : MutableFlareDetector {

    private val _currentFlareState = MutableStateFlow<FlareState>(FlareState.Idle)
    override val currentFlareState: StateFlow<FlareState> = _currentFlareState.asStateFlow()

    private val _registeredFlares = MutableStateFlow<List<Flare>>(emptyList())
    override val registeredFlares: StateFlow<List<Flare>> = _registeredFlares.asStateFlow()

    private var direction: Direction = Direction.DOWN

    private var currGnssData: GnssData? = null

    // Store the GnssData when we first detect the direction change to UP
    private var initialUpAltitude: GnssData? = null

    // Store the GnssData when we first detect the direction change to DOWN
    private var initialDownAltitude: GnssData? = null

    private val flareData = mutableListOf<GnssData>()

    private var count = 0

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("FlareDetectorDelegate") + Dispatchers.Default)

    init {
        exitDetectionFlow
            .flatMapLatest { exit ->
                if (exit == null) {
                    reset()
                    emptyFlow()
                } else {
                    gnssFlow
                }
            }
            .onEach { gnssData ->
                handleNewGnssData(gnssData)
            }
            .launchIn(scope)
    }

    override suspend fun clearAndProcessFlareDetectionData(gnssDataList: List<GnssData>) {
        // Reset detector state
        reset()
        _registeredFlares.value = emptyList()

        if (exitDetectionFlow.value == null) return

        // Process all points up to the rewind index
        gnssDataList
            .forEach { gnssData ->
                handleNewGnssData(gnssData)
            }
    }

    private fun reset() {
        direction = Direction.DOWN
        _currentFlareState.value = FlareState.Idle
        currGnssData = null
        initialUpAltitude = null
        initialDownAltitude = null
        count = 0
        flareData.clear()
    }

    private fun handleNewGnssData(gnssData: GnssData) {
        when (_currentFlareState.value) {
            is FlareState.FlareDone -> {
                reset()
                detectFlareStart(gnssData)
            }
            is FlareState.Flaring -> {
                detectFlareEnd(gnssData)
            }
            FlareState.Idle -> {
                detectFlareStart(gnssData)
            }
        }
    }

    private fun detectFlareStart(gnssData: GnssData) {
        val prevData = currGnssData

        // Store current data for next comparison
        currGnssData = gnssData

        if (prevData == null) {
            // First data point, initialize and wait for more data
            return
        }

        // Check if we've transitioned from going down to going up
        if (direction == Direction.DOWN && gnssData.velD < 0) {
            // We're transitioning to going up, record the start GnssData
            direction = Direction.UP
            initialUpAltitude = prevData
            flareData += prevData
            Timber.v("Direction changed to UP at altitude: ${prevData.hMsl}")
            return
        }

        // If we're already going up, check if we've gained enough altitude to detect a flare
        if (direction == Direction.UP) {
            val startGnssData = initialUpAltitude
            if (startGnssData != null) {
                val altitudeGain = gnssData.hMsl - startGnssData.hMsl
                // Check if we've gained enough altitude to consider it a flare
                if (altitudeGain >= distanceUp) {
                    Timber.i("Flare detected! Gained $altitudeGain meters of altitude since direction change")

                    // Update the flare state to Flaring
                    _currentFlareState.value = FlareState.Flaring(
                        flareData = flareData
                    )
                }
                flareData += prevData
            }
        } else if (gnssData.velD > 0) {
            flareData.clear()
            initialUpAltitude = null
            // Still going down, reset any up detection and ensure direction is DOWN
            direction = Direction.DOWN
        }
    }

    private fun detectFlareEnd(gnssData: GnssData) {
        val prevData = currGnssData
        when (direction) {
            Direction.UP -> {
                val newDirection = if (gnssData.velD > 0) Direction.DOWN else Direction.UP
                if (newDirection == Direction.DOWN) {
                    initialDownAltitude = prevData
                    direction = Direction.DOWN
                    count = 1
                } else {
                    val altitudeGain = gnssData.hMsl - initialUpAltitude!!.hMsl
                    Timber.i("Flare ongoing! Gained $altitudeGain meters of altitude since direction change")
                }
                if (prevData != null) {
                    flareData += prevData
                }
            }
            Direction.DOWN -> {
                val initialDownAlt = initialDownAltitude ?: return
                val newDirection = if (gnssData.velD > 0) Direction.DOWN else Direction.UP
                if (newDirection == Direction.DOWN) {
                    count++
                } else {
                    count = 0
                    direction = Direction.UP
                }
                if (prevData != null) {
                    flareData += prevData
                }
                // Update the flare state to Flaring
                val filteredFlareData = flareData.filter { it.iTow <= initialDownAlt.iTow }
                val altitudeGain = gnssData.hMsl - initialUpAltitude!!.hMsl
                Timber.i("Flare ending. $altitudeGain meters above flare start")
                _currentFlareState.value = FlareState.Flaring(
                    flareData = filteredFlareData
                )
                if (count > 5) {
                    val maxHeight = flareData.maxBy { it.hMsl }
                    val flare = Flare(
                        flareData = flareData.filter { it.iTow <= maxHeight.iTow },
                        gain = maxHeight.hMsl - initialUpAltitude!!.hMsl
                    )
                    Timber.i("Flare ended! start altitude : ${initialUpAltitude!!.hMsl} m, end altitude : ${maxHeight.hMsl} m, gain = ${maxHeight.hMsl - initialUpAltitude!!.hMsl} m")
                    _currentFlareState.value = FlareState.FlareDone(flare)
                    _registeredFlares.value += flare
                    count = 0
                }
            }
        }
        currGnssData = gnssData
    }
}
