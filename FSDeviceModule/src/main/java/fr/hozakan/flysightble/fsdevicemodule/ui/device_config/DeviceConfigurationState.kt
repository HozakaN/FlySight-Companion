package fr.hozakan.flysightble.fsdevicemodule.ui.device_config

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.UnitSystem

@Immutable
data class DeviceConfigurationState(
    val rawConfiguration: String,
    val configuration: ConfigFile,
    val unitSystem: UnitSystem,
    val showConfigAsRaw: Boolean
)