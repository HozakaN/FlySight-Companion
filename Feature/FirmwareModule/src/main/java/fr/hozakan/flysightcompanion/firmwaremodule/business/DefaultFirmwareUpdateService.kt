package fr.hozakan.flysightcompanion.firmwaremodule.business

import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.networkmodule.NetworkService

class DefaultFirmwareUpdateService(
    private val networkService: NetworkService
) : FirmwareUpdateService {

    override suspend fun checkForUpdates(device: FlySightDevice): FirmwareUpdateStatus {
        return FirmwareUpdateStatus.Unknown
    }

}