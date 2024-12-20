package fr.hozakan.flysightble.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightble.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightble.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightble.fsdevicemodule.business.job.PingJob
import fr.hozakan.flysightble.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class BlePingJob(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : PingJob {

    private val resultDeferred = CompletableDeferred<Boolean>()

    override suspend fun ping(timeout: Long): Boolean {
        return scheduler.schedule(
            priority = 1,
            labelProvider = { "Ping" }) {
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
                        if (cmdAcked == Command.PING) {
                            resultDeferred.complete(true)
                        }
                    } else if (cmd == Command.NAK) {
                        val cmdAckedCode = value[1].toInt() and 0xFF
                        val cmdAcked = Command.fromValue(cmdAckedCode)
                        if (cmdAcked == Command.PING) {
                            resultDeferred.complete(false)
                        }
                    }
                }
            }

            gattTaskQueue += FlySightCharacteristic.CRS_TX.uuid to gattCallback

            val writeTask = TaskBuilder.buildPingTask(gatt, gattCharacteristic) {}
            gattTaskQueue.addTask(writeTask)
            val returnValue = try {
                if (timeout > 0L) {
                    withTimeout(timeout) {
                        resultDeferred.await()
                    }
                } else {
                    resultDeferred.await()
                }
            } catch (e: TimeoutCancellationException) {
                false
            } catch (e: Exception) {
//            gattTaskQueue -= gattCallback
                Timber.e(e)
                false
            }
            gattTaskQueue -= gattCallback
            returnValue
        }
    }
}