package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

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
private const val FS_CRS_COMMAND_NAK = 0xf0
private const val FS_CRS_COMMAND_ACK = 0xf1
private const val FS_CRS_COMMAND_PING = 0xfe
private const val FS_CRS_COMMAND_CANCEL = 0xff

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
    NAK(FS_CRS_COMMAND_NAK),
    ACK(FS_CRS_COMMAND_ACK),
    PING(FS_CRS_COMMAND_PING),
    CANCEL(FS_CRS_COMMAND_CANCEL);

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

    fun buildFileDataCommand(packetId: Int, data: ByteArray): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_FILE_DATA.toByte()) + byteArrayOf(packetId.toByte()) + data

    fun buildFileAckCommand(packetId: Int): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_FILE_ACK.toByte()) + byteArrayOf(packetId.toByte())

    fun buildPingCommand(): ByteArray =
        byteArrayOf(FS_CRS_COMMAND_PING.toByte())

}