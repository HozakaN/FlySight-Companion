package fr.hozakan.flysightble.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightble.bluetoothmodule.GattTask
import fr.hozakan.flysightble.framework.extension.bytesToHex
import fr.hozakan.flysightble.model.ble.FlySightCharacteristic

object TaskBuilder {
    fun buildGetDirectoryTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        path: String,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildGetDirectoryCommand(path)
        return GattTask.WriteTask(
            gatt,
            characteristic,
            command,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
            {
                commandLogger(
                    "[COMMAND] [WRITE] [${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }] ${command.bytesToHex()}"
                )
            }
        )
    }

    fun buildReadFileTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        path: String,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildReadFileCommand(path)
        return GattTask.WriteTask(
            gatt,
            characteristic,
            command,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
            {
                commandLogger(
                    "[COMMAND] [WRITE] [${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }] ${command.bytesToHex()}"
                )
            }
        )
    }

    fun buildReadFileAckTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        packetId: Int,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildFileAckCommand(packetId)
        return GattTask.WriteTask(
            gatt,
            characteristic,
            command,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
            {
                commandLogger(
                    "[COMMAND] [WRITE] [${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }] ${command.bytesToHex()}"
                )
            }
        )
    }

    fun buildCreateFileTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        path: String,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildCreateFileCommand(path)
        return GattTask.WriteTask(
            gatt,
            characteristic,
            command,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
            {
                commandLogger(
                    "[COMMAND] [WRITE] [${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }] ${command.bytesToHex()}"
                )
            }
        )
    }

    fun buildWriteFileTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        path: String,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildWriteFileCommand(path)
        return GattTask.WriteTask(
            gatt,
            characteristic,
            command,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
            {
                commandLogger(
                    "[COMMAND] [WRITE] [${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }] ${command.bytesToHex()}"
                )
            }
        )
    }

    fun buildWriteFileDataTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        packetId: Int,
        data: ByteArray,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildFileDataCommand(packetId, data)
        return GattTask.WriteTask(
            gatt,
            characteristic,
            command,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
            {
                commandLogger(
                    "[COMMAND] [WRITE] [${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }] ${command.bytesToHex()}"
                )
            }
        )
    }
}