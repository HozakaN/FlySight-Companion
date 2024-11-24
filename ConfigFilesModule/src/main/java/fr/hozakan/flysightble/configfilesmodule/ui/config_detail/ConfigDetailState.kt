package fr.hozakan.flysightble.configfilesmodule.ui.config_detail

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.UnitSystem

@Immutable
data class ConfigDetailState(
    val configFile: ConfigFile,
    val unitSystem: UnitSystem,
    val configFileFound: Boolean = true,
    val hasValidFileName: Boolean = true,
    val fileSaved: Boolean = false
)