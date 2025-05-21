package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.StateFlow

interface ExitDetector {
    val exitFound: StateFlow<GnssData?>
}