package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.SharedFlow

interface GnssSource {
    val gnssFlow: SharedFlow<GnssData>
}