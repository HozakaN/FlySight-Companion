package fr.hozakan.flysightcompanion.bluetoothmodule

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import kotlinx.coroutines.CompletableDeferred

sealed class GattTask(
    val gatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val commandLogger: () -> Unit,
    val completion: CompletableDeferred<Unit> = CompletableDeferred()
) {
    class ReadTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        commandLogger: () -> Unit,
        completion: CompletableDeferred<Unit> = CompletableDeferred(),
    ) : GattTask(gatt, characteristic, commandLogger, completion)

    class WriteTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        val command: ByteArray,
        val writeType: Int,
        commandLogger: () -> Unit,
        completion: CompletableDeferred<Unit> = CompletableDeferred()
    ) : GattTask(gatt, characteristic, commandLogger, completion) {
        override fun toString(): String {
            return "WriteTask(characteristic=${characteristic.uuid}, command=${command.bytesToHex()}, writeType=$writeType)"
        }
    }

    class WriteDescriptorTask(
        gatt: BluetoothGatt,
        val descriptor: BluetoothGattDescriptor,
        characteristic: BluetoothGattCharacteristic,
        val command: ByteArray,
        commandLogger: () -> Unit,
        completion: CompletableDeferred<Unit> = CompletableDeferred()
    ) : GattTask(gatt, characteristic, commandLogger, completion)
}