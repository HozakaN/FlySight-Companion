package fr.hozakan.flysightcompanion.model


private const val FS_MODE_STATE_SLEEP = 0
private const val FS_MODE_STATE_ACTIVE = 1
private const val FS_MODE_STATE_CONFIG = 2
private const val FS_MODE_STATE_USB = 3
private const val FS_MODE_STATE_PAIRING = 4
private const val FS_MODE_STATE_START = 5

enum class DeviceMode(
    val value: Int
) {
    Sleep(FS_MODE_STATE_SLEEP),
    Active(FS_MODE_STATE_ACTIVE),
    Config(FS_MODE_STATE_CONFIG),
    Usb(FS_MODE_STATE_USB),
    Pairing(FS_MODE_STATE_PAIRING),
    Start(FS_MODE_STATE_START);


    companion object {

        fun fromValue(value: Int): DeviceMode? = entries.firstOrNull { it.value == value }

    }
}
