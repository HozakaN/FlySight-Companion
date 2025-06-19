package fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.framework.coroutine.flow.FlowEvent
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem

@Immutable
data class ConfigDetailState(
    val unitSystem: UnitSystem,
    val editedConfiguration: ConfigFile,
    val configFileFound: Boolean = true,
    val fileSaved: FlowEvent<Boolean>? = null
)