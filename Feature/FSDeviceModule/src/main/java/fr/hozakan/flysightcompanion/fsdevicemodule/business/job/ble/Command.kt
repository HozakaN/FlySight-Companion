package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import fr.hozakan.flysightcompanion.model.ControlPointStatus
import fr.hozakan.flysightcompanion.model.DeviceMode
import java.nio.ByteBuffer

private const val FS_CRS_COMMAND_CREATE = 0x00
private const val FS_CRS_COMMAND_DELETE = 0x01
private const val FS_CRS_COMMAND_READ = 0x02
private const val FS_CRS_COMMAND_WRITE = 0x03
private const val FS_CRS_COMMAND_MK_DIR = 0x04
private const val FS_CRS_COMMAND_READ_DIR = 0x05
private const val FS_CRS_COMMAND_FILE_DATA = 0x10
private const val FS_CRS_COMMAND_FILE_INFO = 0x11
private const val FS_CRS_COMMAND_FILE_ACK = 0x12
private const val FS_CRS_COMMAND_GET_MODE = 0x13
private const val FS_CRS_COMMAND_NAK = 0xf0
private const val FS_CRS_COMMAND_ACK = 0xf1
private const val FS_CRS_COMMAND_PING = 0xfe
private const val FS_CRS_COMMAND_CANCEL = 0xff
private const val FS_CONTROL_COMMAND_START_PISTOL = 0x00
private const val FS_CONTROL_COMMAND_CANCEL_PISTOL = 0x01
private const val SD_CMD_SET_GNSS_BLE_MASK = 0x01
private const val SD_CMD_GET_GNSS_BLE_MASK = 0x02
private const val CP_RESPONSE_ID = 0xF0

enum class Command(val value: Int) {
    CREATE(FS_CRS_COMMAND_CREATE),
    DELETE(FS_CRS_COMMAND_DELETE),
    READ(FS_CRS_COMMAND_READ),
    WRITE(FS_CRS_COMMAND_WRITE),
    MK_DIR(FS_CRS_COMMAND_MK_DIR),
    READ_DIR(FS_CRS_COMMAND_READ_DIR),
    FILE_DATA(FS_CRS_COMMAND_FILE_DATA),
    FILE_INFO(FS_CRS_COMMAND_FILE_INFO),
    FILE_ACK(FS_CRS_COMMAND_FILE_ACK),
    DEVICE_MODE(FS_CRS_COMMAND_GET_MODE),
    NAK(FS_CRS_COMMAND_NAK),
    ACK(FS_CRS_COMMAND_ACK),
    PING(FS_CRS_COMMAND_PING),
    CANCEL(FS_CRS_COMMAND_CANCEL),
    START_GNSS(FS_CONTROL_COMMAND_START_PISTOL),
    STOP_GNSS(FS_CONTROL_COMMAND_CANCEL_PISTOL),
    SET_GNSS_MASK(SD_CMD_SET_GNSS_BLE_MASK),
    GET_GNSS_MASK(SD_CMD_GET_GNSS_BLE_MASK),
    CP_RESPONSE(CP_RESPONSE_ID);

    companion object {

        fun fromValue(value: Int): Command? = entries.firstOrNull { it.value == value }

    }
}

object CommandBuilder {
    fun buildGetDirectoryCommand(path: String): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_READ_DIR.toByte()) + path.toByteArray(Charsets.UTF_8)

    fun buildMkDirCommand(path: String): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_MK_DIR.toByte()) + path.toByteArray(Charsets.UTF_8)

    fun buildCreateFileCommand(path: String): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_CREATE.toByte()) + path.toByteArray(Charsets.UTF_8)

    fun buildDeleteFileCommand(path: String): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_DELETE.toByte()) + path.toByteArray(Charsets.UTF_8)

    fun buildReadFileCommand(path: String): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_READ.toByte()) + ByteBuffer.allocate(4).putInt(0)
            .array() + ByteBuffer.allocate(4).putInt(0).array() + path.toByteArray(
            Charsets.UTF_8
        )

    fun buildWriteFileCommand(path: String): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_WRITE.toByte()) + path.toByteArray(Charsets.UTF_8)

    fun buildStartPistolCommand(): ByteArray =
        byteArrayOf(FS_CONTROL_COMMAND_START_PISTOL.toByte())

    fun buildCancelPistolSCommand(): ByteArray =
        byteArrayOf(FS_CONTROL_COMMAND_CANCEL_PISTOL.toByte())

    fun buildFileDataCommand(packetId: Int, data: ByteArray): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_FILE_DATA.toByte()) + byteArrayOf(packetId.toByte()) + data

    fun buildFileAckCommand(packetId: Int): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_FILE_ACK.toByte()) + byteArrayOf(packetId.toByte())

    fun buildPingCommand(): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_PING.toByte())

    fun buildGetModeCommand(): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_GET_MODE.toByte())

    fun buildSetModeCommand(mode: DeviceMode): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_GET_MODE.toByte()) + byteArrayOf(mode.value.toByte())

    fun buildCancelCommand(): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_CANCEL.toByte())

    fun buildGetMaskCommand(): ByteArray =
        byteArrayOf(SD_CMD_GET_GNSS_BLE_MASK.toByte())

    fun buildSetMaskCommand(mask: UByte): ByteArray =
        byteArrayOf(SD_CMD_SET_GNSS_BLE_MASK.toByte()) + byteArrayOf(mask.toByte())

}