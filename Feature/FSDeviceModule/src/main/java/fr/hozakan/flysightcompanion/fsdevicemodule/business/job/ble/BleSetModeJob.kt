package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.SetModeJob
import fr.hozakan.flysightcompanion.model.ControlPointStatus
import fr.hozakan.flysightcompanion.model.DeviceMode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber

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
        return scheduler.schedule(
            priority = 1,
            labelProvider = { "Set Mode: $mode" }
        ) {
            val gattCallback = object : SimpleBluetoothGattCallback() {
                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    super.onCharacteristicWrite(gatt, characteristic, status)
                    Timber.d("onCharacteristicWrite: ${characteristic?.uuid} status: $status")
                    if (characteristic?.uuid == gattCharacteristic.uuid) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            resultDeferred.complete(true)
                        } else {
                            resultDeferred.complete(false)
                        }
                    }
                }

            }

            gattTaskQueue += gattCharacteristic.uuid to gattCallback

            val task = TaskBuilder.buildSetModeTask(
                gatt,
                gattCharacteristic,
                mode
            ) {
                Timber.d("[COMMAND] [WRITE] Set Mode: $mode")
            }

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
                Timber.d("Set mode timeout: ${e.message}")
                false
            } catch (e: Exception) {
                Timber.e(e, "Set mode error")
                false
            }
            gattTaskQueue -= gattCallback
            returnValue
        }
    }

}

