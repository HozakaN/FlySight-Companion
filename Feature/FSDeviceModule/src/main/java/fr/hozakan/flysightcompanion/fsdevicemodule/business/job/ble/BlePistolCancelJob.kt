package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.PistolCancelJob
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.PistolStartJob
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CompletableDeferred

class BlePistolCancelJob(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : PistolCancelJob {

    private val startAck = CompletableDeferred<Unit>()

    override suspend fun cancel() {
        scheduler.schedule(
            labelProvider = { "Cancel pistol" }
        ) {
            val gattCallback = object : SimpleBluetoothGattCallback() {
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
                        if (cmdAcked == Command.WRITE) {
                            startAck.complete(Unit)
                        }
                    } else if (cmd == Command.NAK) {
                        val cmdAckedCode = value[1].toInt() and 0xFF
                        val cmdAcked = Command.fromValue(cmdAckedCode)
                        if (cmdAcked == Command.WRITE) {
                            startAck.completeExceptionally(Exception("NAK received"))
                        }
                    }
                }
            }

            gattTaskQueue += FlySightCharacteristic.START_CONTROL.uuid to gattCallback
            val writeTask = TaskBuilder.buildCancelPistolTask(
                gatt,
                gattCharacteristic
            ) {}
            gattTaskQueue.addTask(writeTask)
            try {
                startAck.await()
            } catch (e: Exception) {
                gattTaskQueue -= gattCallback
                throw e
            }
            gattTaskQueue -= gattCallback
        }
    }
}