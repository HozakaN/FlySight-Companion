package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.Flare
import kotlinx.coroutines.flow.StateFlow

interface MutableFlareDetector : FlareDetector {
    fun clearAndProcessFlareDetectionData(gnssDataList: List<GnssData>)
}