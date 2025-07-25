package fr.hozakan.flysightcompanion.firmwaremodule.business

import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import kotlinx.coroutines.flow.StateFlow

interface FirmwareUpdateService {
    val firmwareCompatibilityMatrix: StateFlow<FirmwareCompatibilityMatrix>
}