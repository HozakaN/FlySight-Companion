package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.DirectoryWriter
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FileWriter
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class BleDirectoryWriter(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : DirectoryWriter {

    override suspend fun writeDirectory(
        filePath: String
    ): Boolean {
        return scheduler.schedule(
            labelProvider = { "Make dir $filePath" }
        ) {
            val resultDeferred = CompletableDeferred<Boolean>()
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
                        if (cmdAcked == Command.MK_DIR) {
                            resultDeferred.complete(true)
                        }
                    } else if (cmd == Command.NAK) {
                        val cmdAckedCode = value[1].toInt() and 0xFF
                        val cmdAcked = Command.fromValue(cmdAckedCode)
                        if (cmdAcked == Command.MK_DIR) {
                            resultDeferred.complete(false)
                        }
                    }
                }
            }

            gattTaskQueue += FlySightCharacteristic.CRS_TX.uuid to gattCallback

            val writeTask = TaskBuilder.buildPingTask(gatt, gattCharacteristic) {}
            gattTaskQueue.addTask(writeTask)
            val returnValue = try {
                resultDeferred.await()
            } catch (e: Exception) {
                Timber.e(e)
                false
            }
            gattTaskQueue -= gattCallback
            returnValue
        }
    }
}