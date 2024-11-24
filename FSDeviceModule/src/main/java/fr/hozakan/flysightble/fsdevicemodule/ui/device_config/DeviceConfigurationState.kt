package fr.hozakan.flysightble.fsdevicemodule.ui.device_config

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.FileState
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.config.UnitSystem

@Immutable
data class DeviceConfigurationState(
    val configuration: ConfigFile,
    val unitSystem: UnitSystem
)