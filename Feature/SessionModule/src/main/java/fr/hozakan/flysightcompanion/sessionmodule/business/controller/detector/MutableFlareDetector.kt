package fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector

import fr.hozakan.flysightcompanion.model.GnssData

interface MutableFlareDetector : FlareDetector {
    suspend fun clearAndProcessFlareDetectionData(gnssDataList: List<GnssData>)
}