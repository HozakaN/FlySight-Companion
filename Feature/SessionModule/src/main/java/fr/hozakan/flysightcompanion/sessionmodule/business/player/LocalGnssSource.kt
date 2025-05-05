package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class LocalGnssSource : GnssSource {

    private val _gnssFlow = MutableSharedFlow<GnssData>()
    override val gnssFlow: SharedFlow<GnssData> = _gnssFlow.asSharedFlow()

    override val timeMutableSource: TimeMutableSource? = null

}