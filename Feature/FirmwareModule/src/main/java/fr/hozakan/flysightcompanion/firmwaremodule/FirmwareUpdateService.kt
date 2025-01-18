package fr.hozakan.flysightcompanion.firmwaremodule

import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice

interface FirmwareUpdateService {
    suspend fun checkForUpdates(device: FlySightDevice): FirmwareUpdateStatus
}