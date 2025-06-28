package fr.hozakan.flysightcompanion.model


private const val CP_STATUS_SUCCESS = 0x01
private const val CP_STATUS_CMD_NOT_SUPPORTED = 0x02
private const val CP_STATUS_INVALID_PARAMETER = 0x03
private const val CP_STATUS_OPERATION_FAILED = 0x04
private const val CP_STATUS_OPERATION_NOT_PERMITTED = 0x05
private const val CP_STATUS_BUSY = 0x06
private const val CP_STATUS_ERROR_UNKNOWN = 0x07

enum class ControlPointStatus(
    val value: Int
) {
    SUCCESS(CP_STATUS_SUCCESS),
    CMD_NOT_SUPPORTED(CP_STATUS_CMD_NOT_SUPPORTED),
    INVALID_PARAMETER(CP_STATUS_INVALID_PARAMETER),
    OPERATION_FAILED(CP_STATUS_OPERATION_FAILED),
    OPERATION_NOT_PERMITTED(CP_STATUS_OPERATION_NOT_PERMITTED),
    BUSY(CP_STATUS_BUSY),
    ERROR_UNKNOWN(CP_STATUS_ERROR_UNKNOWN);

    companion object Companion {

        fun fromValue(value: Int): ControlPointStatus? = entries.firstOrNull { it.value == value }

    }
}
