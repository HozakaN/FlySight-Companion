package fr.hozakan.flysightble.framework.extension

private const val HEX_PREFIX = "0x"

private fun Byte.byteToRawHex(): String = String.format("%02X", this)

fun ByteArray.bytesToHex(): String {
    val sb = StringBuilder(this.size / 2)
    sb.append(HEX_PREFIX)
    for (byte in this) {
        sb.append(byte.byteToRawHex())
    }
    return sb.toString()
}

private fun String.hexToInt(): Int = Integer.parseUnsignedInt(this.removeHexPrefix(), 16)

private fun String.hexToByte(): Byte = this.hexToInt().toByte()

fun String.hexToBytes(): ByteArray {
    val result = this.removeHexPrefix()
    val size = result.length
    val bytes = ByteArray(size / 2)

    for (i in 0 until size step 2) {
        val digits = result.substring(i, i + 2)
        val byte = digits.hexToByte()
        bytes[i / 2] = byte
    }
    return bytes
}

private fun String.removeHexPrefix(): String =
    if (this.startsWith(HEX_PREFIX)) this.substring(2) else this



fun ByteArray.toInt(): Int {
    var value = 0
    for (b in this) {
        value = (value shl 8) + (b.toInt() and 0xFF)
    }
    return value
}