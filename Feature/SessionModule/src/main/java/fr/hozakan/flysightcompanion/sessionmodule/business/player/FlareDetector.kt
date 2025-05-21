package fr.hozakan.flysightcompanion.sessionmodule.business.player

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
        val flareData: List<GnssData>
    ) : FlareState
    data class FlareDone(
        val flare: Flare
    ) : FlareState
}