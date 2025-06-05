package fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector

import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.StateFlow

interface ExitDetector {
    val exitFound: StateFlow<GnssData?>
}