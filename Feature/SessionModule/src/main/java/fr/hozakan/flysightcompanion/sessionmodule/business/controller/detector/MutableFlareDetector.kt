package fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector

import fr.hozakan.flysightcompanion.model.GnssData

interface MutableFlareDetector : FlareDetector {
    fun handleNewData(gnssData: GnssData)
    suspend fun clear()
}