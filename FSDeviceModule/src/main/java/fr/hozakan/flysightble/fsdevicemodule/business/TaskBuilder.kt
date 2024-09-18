package fr.hozakan.flysightble.fsdevicemodule.business

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightble.framework.extension.bytesToHex

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

    fun buildReadConfigFileTask(
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
}