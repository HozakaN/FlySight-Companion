package fr.hozakan.flysightcompanion.model.firmware

sealed interface FirmwareUpdateStatus {
    /**
     * Downloading the firmware from internet
     */
    data object Downloading : FirmwareUpdateStatus

    /**
     * Pushing the firmware onto the FlySight
     */
    data object Pushing : FirmwareUpdateStatus

    data class PushingWithAmount(
        val maxValue: Int,
        val currentValue: Int
    ) : FirmwareUpdateStatus

    /**
     * Disconnecting from Bluetooth
     */
    data object DisconnectingFromBluetooth : FirmwareUpdateStatus

    /**
     * Awaiting for the user to plug the FlySight to the phone through USB
     */
    data object AwaitingUsbConnection : FirmwareUpdateStatus

    /**
     * Awaiting for the user to push the power button until the LED becomes orange.
     * This state should end when the FlySight usb is detached : then reattached
     */
    data object AwaitingButtonPush : FirmwareUpdateStatus

    /**
     * Ask the user to disconnect the cable from the phone. This state should end when the phone receives a detached state callback
     */
    data object DisconnectingFromUsb : FirmwareUpdateStatus

    /**
     * Then trying to reconnect to the FlySight through Bluetooth
     */
    data object AwaitingBluetoothReconnection : FirmwareUpdateStatus

    /**
     * Waiting for the flysight.txt file to be fetched so we can check the firmware version
     */
    data object FirmwareVersionCheck : FirmwareUpdateStatus

    /**
     * The firmware update is done
     */
    data object Done : FirmwareUpdateStatus

    /**
     * Actually, there is no firmware update to do
     */
    data object NoUpdate : FirmwareUpdateStatus

    /**
     * The firmware update has encountered an error
     */
    data class Error(val errorInfo: ErrorInfo) : FirmwareUpdateStatus

    sealed interface ErrorInfo {
        data object FirmwareVersionCheckError : ErrorInfo
        data object FirmwareVersionCheckTimeOut : ErrorInfo
        data object CantReconnect : ErrorInfo
        data object DownloadError : ErrorInfo
        data object PushFirmwareError : ErrorInfo
        data object Unknown : ErrorInfo
    }
}
