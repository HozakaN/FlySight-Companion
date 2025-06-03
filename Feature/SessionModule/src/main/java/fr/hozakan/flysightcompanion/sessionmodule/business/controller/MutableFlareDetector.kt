package fr.hozakan.flysightcompanion.sessionmodule.business.controller

import fr.hozakan.flysightcompanion.model.GnssData

interface MutableFlareDetector : FlareDetector {
    suspend fun clearAndProcessFlareDetectionData(gnssDataList: List<GnssData>)
}