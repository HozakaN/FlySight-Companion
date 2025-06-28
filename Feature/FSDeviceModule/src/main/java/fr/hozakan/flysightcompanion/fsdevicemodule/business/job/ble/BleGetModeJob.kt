package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTask
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.GetModeJob
import fr.hozakan.flysightcompanion.model.ControlPointStatus
import fr.hozakan.flysightcompanion.model.DeviceMode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class BleGetModeJob(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : GetModeJob {

    private var counter = 0

    override suspend fun getMode(timeout: Long): DeviceMode {
        val resultDeferred = CompletableDeferred<DeviceMode>()
        return scheduler.schedule( // ping job is high priority and should not used a scheduler
            priority = 1,
            labelProvider = { "Get Mode" }) {
            val gattCallback = object : SimpleBluetoothGattCallback() {
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray,
                    status: Int
                ) {
                    super.onCharacteristicRead(gatt, characteristic, value, status)
                    Timber.d("onCharacteristicRead: ${characteristic.uuid} (gattCharacteristic.uuid is ${gattCharacteristic.uuid}) ${value.bytesToHex()}")
                    if (characteristic.uuid == gattCharacteristic.uuid) {
                        val modeValue = value[0].toInt()
                        val mode = DeviceMode.fromValue(modeValue)
                        if (mode != null) {
                            resultDeferred.complete(mode)
                        } else {
                            resultDeferred.completeExceptionally(IllegalStateException("Unknown mode value: $modeValue"))
                        }
                    }
                }
            }

            gattTaskQueue += gattCharacteristic.uuid to gattCallback

            val task = GattTask.ReadTask(gatt, gattCharacteristic, {})

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
                Timber.d(e.toString())
                DeviceMode.Sleep
            } catch (e: Exception) {
                Timber.e(e)
                DeviceMode.Sleep
            }
            gattTaskQueue -= gattCallback
            returnValue
        }
    }

}

