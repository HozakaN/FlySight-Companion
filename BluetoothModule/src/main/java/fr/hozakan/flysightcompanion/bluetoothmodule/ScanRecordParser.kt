package fr.hozakan.flysightcompanion.bluetoothmodule

import android.bluetooth.le.ScanRecord.DATA_TYPE_FLAGS
import android.bluetooth.le.ScanRecord.DATA_TYPE_LOCAL_NAME_COMPLETE
import android.bluetooth.le.ScanRecord.DATA_TYPE_LOCAL_NAME_SHORT
import android.bluetooth.le.ScanRecord.DATA_TYPE_MANUFACTURER_SPECIFIC_DATA
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_DATA_128_BIT
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_DATA_16_BIT
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_DATA_32_BIT
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE
import android.bluetooth.le.ScanRecord.DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL
import android.bluetooth.le.ScanRecord.DATA_TYPE_TRANSPORT_DISCOVERY_DATA
import android.bluetooth.le.ScanRecord.DATA_TYPE_TX_POWER_LEVEL
import android.bluetooth.le.TransportDiscoveryData
import android.os.ParcelUuid
import android.util.ArrayMap
import android.util.SparseArray
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID


class ScanRecordParser {
    companion object {

        private const val UUID_BYTES_16_BIT: Int = 2
        private const val UUID_BYTES_32_BIT: Int = 4
        private const val UUID_BYTES_128_BIT: Int = 16
        private val BASE_UUID: ParcelUuid =
            ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB")

        fun parseFromBytes(scanRecord: ByteArray?): Map<Int, ByteArray>? {
            if (scanRecord == null) {
                return null
            }

            var currentPos = 0
            var advertiseFlag = -1
            var serviceUuids: List<ParcelUuid>? = ArrayList()
            val serviceSolicitationUuids: List<ParcelUuid> = ArrayList()
            var localName: String? = null
            var txPowerLevel = Int.MIN_VALUE

            val manufacturerData = SparseArray<ByteArray>()
            val serviceData: MutableMap<ParcelUuid, ByteArray> = ArrayMap<ParcelUuid, ByteArray>()
            val advertisingDataMap = HashMap<Int, ByteArray>()

            var transportDiscoveryData: TransportDiscoveryData? = null

            try {
                while (currentPos < scanRecord.size) {
                    // length is unsigned int.
                    val length = scanRecord[currentPos++].toInt() and 0xFF
                    if (length == 0) {
                        break
                    }
                    // Note the length includes the length of the field type itself.
                    val dataLength = length - 1
                    // fieldType is unsigned int.
                    val fieldType = scanRecord[currentPos++].toInt() and 0xFF
                    val advertisingData: ByteArray =
                        extractBytes(scanRecord, currentPos, dataLength)
                    advertisingDataMap[fieldType] = advertisingData
                    when (fieldType) {
                        DATA_TYPE_FLAGS -> advertiseFlag = scanRecord[currentPos].toInt() and 0xFF
                        DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord,
                            currentPos,
                            dataLength,
                            UUID_BYTES_16_BIT,
                            serviceUuids?.filterNotNull()?.toMutableList() ?: mutableListOf()
                        )

                        DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord,
                            currentPos,
                            dataLength,
                            UUID_BYTES_32_BIT,
                            serviceUuids?.toMutableList() ?: mutableListOf()
                        )

                        DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord,
                            currentPos,
                            dataLength,
                            UUID_BYTES_128_BIT,
                            serviceUuids?.toMutableList() ?: mutableListOf()
                        )

                        DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT -> parseServiceSolicitationUuid(
                            scanRecord,
                            currentPos,
                            dataLength,
                            UUID_BYTES_16_BIT,
                            serviceSolicitationUuids.toMutableList()
                        )

                        DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT -> parseServiceSolicitationUuid(
                            scanRecord,
                            currentPos,
                            dataLength,
                            UUID_BYTES_32_BIT,
                            serviceSolicitationUuids.toMutableList()
                        )

                        DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT -> parseServiceSolicitationUuid(
                            scanRecord,
                            currentPos,
                            dataLength,
                            UUID_BYTES_128_BIT,
                            serviceSolicitationUuids.toMutableList()
                        )

                        DATA_TYPE_LOCAL_NAME_SHORT, DATA_TYPE_LOCAL_NAME_COMPLETE -> localName =
                            String(extractBytes(scanRecord, currentPos, dataLength))

                        DATA_TYPE_TX_POWER_LEVEL -> txPowerLevel = scanRecord[currentPos].toInt()
                        DATA_TYPE_SERVICE_DATA_16_BIT, DATA_TYPE_SERVICE_DATA_32_BIT, DATA_TYPE_SERVICE_DATA_128_BIT -> {
                            var serviceUuidLength: Int = UUID_BYTES_16_BIT
                            if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
                                serviceUuidLength = UUID_BYTES_32_BIT
                            } else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
                                serviceUuidLength = UUID_BYTES_128_BIT
                            }

                            val serviceDataUuidBytes: ByteArray =
                                extractBytes(scanRecord, currentPos, serviceUuidLength)
                            val serviceDataUuid: ParcelUuid =
                                parseUuidFrom(serviceDataUuidBytes)
                            val serviceDataArray: ByteArray =
                                extractBytes(
                                    scanRecord,
                                    currentPos + serviceUuidLength,
                                    dataLength - serviceUuidLength
                                )
                            serviceData[serviceDataUuid] = serviceDataArray
                        }

                        DATA_TYPE_MANUFACTURER_SPECIFIC_DATA -> {
                            // The first two bytes of the manufacturer specific data are
                            // manufacturer ids in little endian.
                            val manufacturerId =
                                (((scanRecord[currentPos + 1].toInt() and 0xFF) shl 8)
                                        + (scanRecord[currentPos].toInt() and 0xFF))
                            val manufacturerDataBytes: ByteArray =
                                extractBytes(scanRecord, currentPos + 2, dataLength - 2)
//                            if (Flags.scanRecordManufacturerDataMerge()) {
//                                if (manufacturerData.contains(manufacturerId)) {
//                                    val firstValue = manufacturerData[manufacturerId]
//                                    val buffer =
//                                        ByteBuffer.allocate(
//                                            firstValue.size + manufacturerDataBytes.size
//                                        )
//                                    buffer.put(firstValue)
//                                    buffer.put(manufacturerDataBytes)
//                                    manufacturerData.put(manufacturerId, buffer.array())
//                                } else {
//                                    manufacturerData.put(manufacturerId, manufacturerDataBytes)
//                                }
//                            } else {
                                manufacturerData.put(manufacturerId, manufacturerDataBytes)
//                            }
                        }

                        DATA_TYPE_TRANSPORT_DISCOVERY_DATA -> {
                            // -1 / +1 to include the type in the extract
                            val transportDiscoveryDataBytes: ByteArray =
                                extractBytes(scanRecord, currentPos - 1, dataLength + 1)
//                            transportDiscoveryData =
//                                TransportDiscoveryData(transportDiscoveryDataBytes)
                        }

                        else -> {}
                    }
                    currentPos += dataLength
                }

                if (serviceUuids!!.isEmpty()) {
                    serviceUuids = null
                }
                return advertisingDataMap
            } catch (e: Exception) {
                Timber.e("unable to parse scan record: " + scanRecord.contentToString())
                // As the record is invalid, ignore all the parsed results for this packet
                // and return an empty record with raw scanRecord bytes in results
                return advertisingDataMap
            }
        }

        // Helper method to extract bytes from byte array.
        private fun extractBytes(scanRecord: ByteArray, start: Int, length: Int): ByteArray {
            val bytes = ByteArray(length)
            System.arraycopy(scanRecord, start, bytes, 0, length)
            return bytes
        }

        // Parse service UUIDs.
        private fun parseServiceUuid(
            scanRecord: ByteArray,
            currentPos: Int,
            dataLength: Int,
            uuidLength: Int,
            serviceUuids: MutableList<ParcelUuid>
        ): Int {
            var currentPos = currentPos
            var dataLength = dataLength
            while (dataLength > 0) {
                val uuidBytes = extractBytes(scanRecord, currentPos, uuidLength)
                serviceUuids.add(parseUuidFrom(uuidBytes))
                dataLength -= uuidLength
                currentPos += uuidLength
            }
            return currentPos
        }

        private fun parseServiceSolicitationUuid(
            scanRecord: ByteArray,
            currentPos: Int,
            dataLength: Int,
            uuidLength: Int,
            serviceSolicitationUuids: MutableList<ParcelUuid>
        ): Int {
            var currentPos = currentPos
            var dataLength = dataLength
            while (dataLength > 0) {
                val uuidBytes = extractBytes(scanRecord, currentPos, uuidLength)
                serviceSolicitationUuids.add(parseUuidFrom(uuidBytes))
                dataLength -= uuidLength
                currentPos += uuidLength
            }
            return currentPos
        }

        private fun parseUuidFrom(uuidBytes: ByteArray?): ParcelUuid {
            requireNotNull(uuidBytes) { "uuidBytes cannot be null" }
            val length = uuidBytes.size
            require(!(length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT && length != UUID_BYTES_128_BIT)) { "uuidBytes length invalid - $length" }

            // Construct a 128 bit UUID.
            if (length == UUID_BYTES_128_BIT) {
                val buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
                val msb = buf.getLong(8)
                val lsb = buf.getLong(0)
                return ParcelUuid(UUID(msb, lsb))
            }

            // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
            // 128_bit_value = uuid * 2^96 + BASE_UUID
            var shortUuid: Long
            if (length == UUID_BYTES_16_BIT) {
                shortUuid = (uuidBytes[0].toInt() and 0xFF).toLong()
                shortUuid += ((uuidBytes[1].toInt() and 0xFF) shl 8).toLong()
            } else {
                shortUuid = (uuidBytes[0].toInt() and 0xFF).toLong()
                shortUuid += ((uuidBytes[1].toInt() and 0xFF) shl 8).toLong()
                shortUuid += ((uuidBytes[2].toInt() and 0xFF) shl 16).toLong()
                shortUuid += ((uuidBytes[3].toInt() and 0xFF) shl 24).toLong()
            }
            val msb: Long = BASE_UUID.uuid.mostSignificantBits + (shortUuid shl 32)
            val lsb: Long = BASE_UUID.uuid.leastSignificantBits
            return ParcelUuid(UUID(msb, lsb))
        }
    }

}