package fr.hozakan.flysightcompanion.firmwaremodule.business

sealed interface FirmwareUpdateStatus {
    data object Unknown : FirmwareUpdateStatus
    data object NoUpdateAvailable : FirmwareUpdateStatus
    data class UpdateAvailable(val version: String) : FirmwareUpdateStatus
}