package fr.hozakan.flysightcompanion.model.ble

import java.util.UUID

sealed class FlySightCharacteristic(val name: String, val uuid: UUID) {
    data object CRS_RX : FlySightCharacteristic("CRS_RX", CRS_RX_UUID)
    data object CRS_TX : FlySightCharacteristic("CRS_TX", CRS_TX_UUID)
    data object GNSS_PV : FlySightCharacteristic("GNSS_PV", GNSS_PV_UUID)
    data object START_CONTROL : FlySightCharacteristic("START_CONTROL", START_CONTROL_UUID)
    data object START_RESULT : FlySightCharacteristic("START_RESULT", START_RESULT_UUID)
    data object BATTERY : FlySightCharacteristic("BATTERY", batteryLevelCharUuid)
    companion object {
        fun values(): List<FlySightCharacteristic> {
            return listOf(
                CRS_TX,
                CRS_RX,
                GNSS_PV,
                START_CONTROL,
                START_RESULT,
                BATTERY
            )
        }

        fun fromUuid(uuid: UUID?): FlySightCharacteristic? {
            return when (uuid) {
                CRS_RX_UUID -> CRS_RX
                CRS_TX_UUID -> CRS_TX
                GNSS_PV_UUID -> GNSS_PV
                START_CONTROL_UUID -> START_CONTROL
                START_RESULT_UUID -> START_RESULT
                batteryLevelCharUuid -> BATTERY
                else -> null
            }
        }
    }
}

val CRS_RX_UUID = UUID.fromString("00000002-8e22-4541-9d4c-21edae82ed19")
val CRS_TX_UUID = UUID.fromString("00000001-8e22-4541-9d4c-21edae82ed19")
val GNSS_PV_UUID = UUID.fromString("00000000-8e22-4541-9d4c-21edae82ed19")
val START_CONTROL_UUID = UUID.fromString("00000003-8e22-4541-9d4c-21edae82ed19")
val START_RESULT_UUID = UUID.fromString("00000004-8e22-4541-9d4c-21edae82ed19")
val batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
val batteryLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")