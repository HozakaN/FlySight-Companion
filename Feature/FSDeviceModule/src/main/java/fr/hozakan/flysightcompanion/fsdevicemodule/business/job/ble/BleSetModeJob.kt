package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTask
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.GetModeJob
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.SetModeJob
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.time.LocalDateTime
import kotlin.time.Duration

class BleSetModeJob(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : SetModeJob {

    override suspend fun setMode(
        mode: DeviceMode,
        timeout: Long
    ): Boolean {
        val resultDeferred = CompletableDeferred<Boolean>()
        return scheduler.schedule( // ping job is high priority and should not used a scheduler
            priority = 1,
            labelProvider = { "Set Mode" }) {
            val gattCallback = object : SimpleBluetoothGattCallback() {
                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    super.onCharacteristicChanged(gatt, characteristic, value)
                    Timber.d("onCharacteristicChanged: ${characteristic.uuid} (gattCharacteristic.uuid is ${gattCharacteristic.uuid}) ${value.bytesToHex()}")
                    val cmdCode = value[0].toInt() and 0xFF
                    val cmd = Command.fromValue(cmdCode)
                    if (cmd == Command.ACK) {
                        val cmdAckedCode = value[1].toInt() and 0xFF
                        val cmdAcked = Command.fromValue(cmdAckedCode)
                        if (cmdAcked == Command.DEVICE_MODE) {
                            resultDeferred.complete(true)
                        }
                    } else if (cmd == Command.NAK) {
                        val cmdAckedCode = value[1].toInt() and 0xFF
                        val cmdAcked = Command.fromValue(cmdAckedCode)
                        if (cmdAcked == Command.DEVICE_MODE) {
                            resultDeferred.complete(false)
                        }
                    }
                }
            }

            gattTaskQueue += gattCharacteristic.uuid to gattCallback

            val task = TaskBuilder.buildSetModeTask(gatt, gattCharacteristic, mode) {}

            gattTaskQueue.addTask(task)
            val returnValue = try {
                if (timeout > 0L) {
                    withTimeout(timeout) {
                        resultDeferred.await()
                    }
                } else {
                    resultDeferred.await()
                }
            } catch (e: TimeoutCancellationException) {
                Timber.i(e.toString())
                false
            } catch (e: Exception) {
                Timber.e(e)
                false
            }
            gattTaskQueue -= gattCallback
            returnValue
        }
    }

}

