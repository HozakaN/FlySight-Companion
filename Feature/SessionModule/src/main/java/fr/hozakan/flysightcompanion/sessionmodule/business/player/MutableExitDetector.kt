package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData

interface MutableExitDetector : ExitDetector {
    suspend fun clearAndProcessExitDetectionData(gnssData: List<GnssData>)
}