package fr.hozakan.flysightcompanion.model.firmware

enum class FirmwareUpdateStatus {
    /**
     * Downloading the firmware from internet
     */
    Downloading,

    /**
     * Pushing the firmware onto the FlySight
     */
    Pushing,

    /**
     * Disconnecting from Bluetooth
     */
    DisconnectingFromBluetooth,

    /**
     * Awaiting for the user to plug the FlySight to the phone through USB
     */
    AwaitingUsbConnection,

    /**
     * Awaiting for the user to push the power button until the LED becomes orange.
     * This state should end when the FlySight usb is detached, then reattached
     */
    AwaitingButtonPush,

    /**
     * Ask the user to disconnect the cable from the phone. This state should end when the phone receives a detached state callback
     */
    DisconnectingFromUsb,

    /**
     * Then trying to reconnect to the FlySight through Bluetooth
     */
    AwaitingBluetoothReconnection,

    /**
     * Waiting for the flysight.txt file to be fetched so we can check the firmware version
     */
    FirmwareVersionCheck,

    /**
     * The firmware update is done
     */
    Done,

    /**
     * Actually, there is no firmware update to do
     */
    NoUpdate,

    /**
     * The firmware update has encountered an error
     */
    Error
}