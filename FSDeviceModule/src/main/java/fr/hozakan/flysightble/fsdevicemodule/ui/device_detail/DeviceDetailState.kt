package fr.hozakan.flysightble.fsdevicemodule.ui.device_detail

import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.FileInfo

data class DeviceDetailState(
    val device: FlySightDevice?,
    val currentDirectoryPath: List<String>,
    val directoryContent: List<FileInfo>
)