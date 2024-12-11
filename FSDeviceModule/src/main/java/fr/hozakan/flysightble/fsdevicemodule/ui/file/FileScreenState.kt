package fr.hozakan.flysightble.fsdevicemodule.ui.file

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.FileState

@Immutable
data class FileScreenState(
    val device: FlySightDevice?,
    val fileContent: FileState
)