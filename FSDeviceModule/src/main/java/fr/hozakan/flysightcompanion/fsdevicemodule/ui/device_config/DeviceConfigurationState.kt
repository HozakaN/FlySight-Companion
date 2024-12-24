package fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_config

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem

@Immutable
data class DeviceConfigurationState(
    val rawConfiguration: String,
    val configuration: ConfigFile,
    val unitSystem: UnitSystem,
    val showConfigAsRaw: Boolean
)