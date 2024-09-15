package fr.hozakan.flysightble.fsdevicemodule.business

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
    CANCEL(FS_CRS_COMMAND_CANCEL);

    companion object {

        fun fromValue(value: Int): Command? = when (value) {
            FS_CRS_COMMAND_CREATE -> CREATE
            FS_CRS_COMMAND_DELETE -> DELETE
            FS_CRS_COMMAND_READ -> READ
            FS_CRS_COMMAND_WRITE -> WRITE
            FS_CRS_COMMAND_MK_DIR -> MK_DIR
            FS_CRS_COMMAND_READ_DIR -> READ_DIR
            FS_CRS_COMMAND_FILE_DATA -> FILE_DATA
            FS_CRS_COMMAND_FILE_INFO -> FILE_INFO
            FS_CRS_COMMAND_FILE_ACK -> FILE_ACK
            FS_CRS_COMMAND_NAK -> NAK
            FS_CRS_COMMAND_ACK -> ACK
            FS_CRS_COMMAND_CANCEL -> CANCEL
            else -> null
        }

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

}