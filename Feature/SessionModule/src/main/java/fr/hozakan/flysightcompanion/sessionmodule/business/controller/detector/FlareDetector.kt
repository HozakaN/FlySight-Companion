package fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector

import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.Flare
import kotlinx.coroutines.flow.StateFlow

interface FlareDetector {

    val currentFlareState: StateFlow<FlareState>
    val registeredFlares: StateFlow<List<Flare>>
}

sealed interface FlareState {
    data object Idle : FlareState
    data class Flaring(
        val flareData: List<GnssData>,
        val startAltitude: Int,
        val startTime: Int,
        val timeSinceStart: Int,
        val altitudeGain: Int
    ) : FlareState
    data class FlareDone(
        val flare: Flare
    ) : FlareState
}