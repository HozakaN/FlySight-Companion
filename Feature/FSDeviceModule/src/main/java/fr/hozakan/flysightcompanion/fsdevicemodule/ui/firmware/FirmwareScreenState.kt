package fr.hozakan.flysightcompanion.fsdevicemodule.ui.firmware

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix

@Immutable
data class FirmwareScreenState(
    val device: FlySightDevice?,
    val compatibilityMatrix: FirmwareCompatibilityMatrix,
    val currentAppVersion: String,
    val currentFirmwareVersion: String?,
    val currentStackVersion: String?,
)