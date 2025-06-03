package fr.hozakan.flysightcompanion.sessionmodule.business.controller

import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.StateFlow

interface ExitDetector {
    val exitFound: StateFlow<GnssData?>
}