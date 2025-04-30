package fr.hozakan.flysightcompanion.sessionmodule.business.player

import android.content.Context
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FileGnssSource(
    private val context: Context,
    private val fileName: String
) : GnssSource {

    private val _gnssFlow = MutableSharedFlow<GnssData>()
    override val gnssFlow: SharedFlow<GnssData> = _gnssFlow.asSharedFlow()

}