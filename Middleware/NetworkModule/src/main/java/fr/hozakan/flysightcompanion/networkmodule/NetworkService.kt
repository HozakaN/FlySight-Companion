package fr.hozakan.flysightcompanion.networkmodule

import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo
import fr.hozakan.flysightcompanion.model.firmware.FirmwareVersion
import kotlinx.coroutines.flow.StateFlow

interface NetworkService {
    //    suspend fun getAvailableFirmwares(): List<FirmwareVersion>
//    val firmwares: StateFlow<List<FirmwareVersion>>
    suspend fun getFirmwareCompatibilityMatrix(): FirmwareCompatibilityMatrix?
    suspend fun downloadFirmware(deviceBatch: String, firmwareInfo: FirmwareInfo): ByteArray?
    suspend fun downloadStack(stackVersion: String): ByteArray?
}