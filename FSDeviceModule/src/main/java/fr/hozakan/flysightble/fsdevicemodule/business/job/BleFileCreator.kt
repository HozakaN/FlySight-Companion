package fr.hozakan.flysightble.fsdevicemodule.business.job

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightble.fsdevicemodule.business.Command
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightCharacteristic
import fr.hozakan.flysightble.fsdevicemodule.business.GattTaskQueue
import fr.hozakan.flysightble.fsdevicemodule.business.TaskBuilder
import kotlinx.coroutines.CompletableDeferred

class BleFileCreator(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue
) : FileCreator {

    override suspend fun createFile(filePath: String) {
        val createAck = CompletableDeferred<Unit>()
        val gattCallback = object : BluetoothGattCallback() {
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)
                val cmdCode = value[0].toInt() and 0xFF
                val cmd = Command.fromValue(cmdCode)
                if (cmd == Command.ACK) {
                    val cmdAckedCode = value[1].toInt() and 0xFF
                    val cmdAcked = Command.fromValue(cmdAckedCode)
                    if (cmdAcked == Command.CREATE) {
                        createAck.complete(Unit)
                    }
                } else if (cmd == Command.NAK) {
                    val cmdAckedCode = value[1].toInt() and 0xFF
                    val cmdAcked = Command.fromValue(cmdAckedCode)
                    if (cmdAcked == Command.CREATE) {
                        createAck.completeExceptionally(Exception("NAK received"))
                    }
                }
            }
        }

        gattTaskQueue += FlySightCharacteristic.CRS_TX.uuid to gattCallback
        val createFileTask = TaskBuilder.buildCreateFileTask(gatt, gattCharacteristic, filePath) {}
        gattTaskQueue.addTask(createFileTask)
        try {
            createAck.await()
        } catch (e: Exception) {
            gattTaskQueue -= gattCallback
            throw e
        }
        gattTaskQueue -= gattCallback
    }

}