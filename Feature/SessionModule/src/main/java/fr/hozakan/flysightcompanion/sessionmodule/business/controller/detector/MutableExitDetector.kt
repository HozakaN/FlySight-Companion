package fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector

import fr.hozakan.flysightcompanion.model.GnssData

interface MutableExitDetector : ExitDetector {
    fun handleNewData(gnssData: GnssData)
    suspend fun clear()
}