package fr.hozakan.flysightble.configfilesmodule.ui.config_detail

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.UnitSystem

@Immutable
data class ConfigDetailState(
    val configFile: ConfigFile,
    val unitSystem: UnitSystem,
    val editedConfiguration: ConfigFile? = null,
    val configFileFound: Boolean = true,
    val hasValidFileName: Boolean = true,
    val fileSaved: FlowEvent<Boolean>? = null
)