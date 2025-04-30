package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.SharedFlow

class FlySightGnssSource(
    fsDevice: FlySightDevice
) : GnssSource {

    override val gnssFlow: SharedFlow<GnssData> = fsDevice.gnssFeed

}