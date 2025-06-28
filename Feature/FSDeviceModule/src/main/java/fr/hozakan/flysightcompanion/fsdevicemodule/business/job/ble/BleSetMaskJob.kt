package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTask
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.SetMaskJob
import fr.hozakan.flysightcompanion.model.ControlPointStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class BleSetMaskJob(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : SetMaskJob {

    override suspend fun setMask(
        mask: UByte,
        timeout: Long
    ): Boolean {
        val resultDeferred = CompletableDeferred<Boolean>()
        return scheduler.schedule(
            priority = 1,
            labelProvider = { "Set GNSS Mask" }) {
            val gattCallback = object : SimpleBluetoothGattCallback() {
                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    super.onCharacteristicChanged(gatt, characteristic, value)
                    Timber.d("onCharacteristicChanged: ${characteristic.uuid} (gattCharacteristic.uuid is ${gattCharacteristic.uuid}) ${value.bytesToHex()}")

                    if (characteristic.uuid == gattCharacteristic.uuid && value.isNotEmpty()) {
                        val responseId = value[0].toInt() and 0xFF
                        if (responseId == Command.CP_RESPONSE.value && value.size >= 3) {
                            val originalCmd = value[1].toInt() and 0xFF
                            val statusInt = value[2].toInt() and 0xFF
                            val status = ControlPointStatus.fromValue(statusInt)

                            if (originalCmd == Command.SET_GNSS_MASK.value) {
                                if (status == ControlPointStatus.SUCCESS) {
                                    resultDeferred.complete(true)
                                } else {
                                    resultDeferred.complete(false)
                                }
                            }
                        }
                    }
                }
            }

            gattTaskQueue += gattCharacteristic.uuid to gattCallback

            val task = TaskBuilder.buildSetGnssMaskTask(gatt, gattCharacteristic, mask) {}

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