package fr.hozakan.flysightble.fsdevicemodule.ui.device_detail

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.FileInfo

@Immutable
data class DeviceDetailState(
    val device: FlySightDevice?,
    val currentDirectoryPath: List<String>,
    val directoryContent: List<FileInfo>,
    val configFileInfo: FileInfo?
)