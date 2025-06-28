package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTask
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.model.ControlPointStatus
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic

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

    fun buildCancelReadTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildCancelCommand()
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

    fun buildStartPistolTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildStartPistolCommand()
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

    fun buildCancelPistolTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildCancelPistolSCommand()
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

    fun buildMakeDirTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        path: String,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildMkDirCommand(path)
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

    fun buildPingTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildPingCommand()
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

    fun buildGetModeTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildGetModeCommand()
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

    fun buildSetModeTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        deviceMode: DeviceMode,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildSetModeCommand(deviceMode)
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

    fun buildSetGnssMaskTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        mask: UByte,
        commandLogger: (String) -> Unit
    ) : GattTask {
        val command = CommandBuilder.buildSetMaskCommand(mask)
        return GattTask.WriteTask(
            gatt,
            characteristic,
            command,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
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