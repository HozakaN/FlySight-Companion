package fr.hozakan.flysightcompanion.fsdevicemodule.ui.file

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.FileState

@Immutable
data class FileScreenState(
    val device: FlySightDevice?,
    val fileContent: FileState
)