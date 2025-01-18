package fr.hozakan.flysightcompanion.firmwaremodule

sealed interface FirmwareUpdateStatus {
    data object Unknown : FirmwareUpdateStatus
    data object NoUpdateAvailable : FirmwareUpdateStatus
    data class UpdateAvailable(val version: String) : FirmwareUpdateStatus
}