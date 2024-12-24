package fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.FileInfo

@Immutable
data class DeviceDetailState(
    val device: FlySightDevice?,
    val currentDirectoryPath: List<String>,
    val directoryContent: List<FileInfo>,
    val configFileInfo: FileInfo?,
    val configFile: FileState,
    val fileClicked: FlowEvent<List<String>>?
)