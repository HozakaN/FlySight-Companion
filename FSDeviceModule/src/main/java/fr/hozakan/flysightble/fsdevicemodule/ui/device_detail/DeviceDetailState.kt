package fr.hozakan.flysightble.fsdevicemodule.ui.device_detail

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.FileState
import fr.hozakan.flysightble.model.FileInfo

@Immutable
data class DeviceDetailState(
    val device: FlySightDevice?,
    val currentDirectoryPath: List<String>,
    val directoryContent: List<FileInfo>,
    val configFileInfo: FileInfo?,
    val configFile: FileState,
    val fileClicked: FlowEvent<List<String>>?
)