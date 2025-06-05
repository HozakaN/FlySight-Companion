package fr.hozakan.flysightcompanion.sessionmodule.business.controller.source

import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.SharedFlow

interface GnssSource {
    val gnssFlow: SharedFlow<GnssData>
    val timeMutableSource: TimeMutableSource?
}