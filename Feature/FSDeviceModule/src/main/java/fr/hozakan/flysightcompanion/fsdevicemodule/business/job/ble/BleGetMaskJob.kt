package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTask
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.GetMaskJob
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class BleGetMaskJob(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : GetMaskJob {

    override suspend fun getMask(timeout: Long): UByte {
        val resultDeferred = CompletableDeferred<UByte>()
        return 1.toUByte()
//        return scheduler.schedule(
//            priority = 1,
//            labelProvider = { "Get GNSS Mask" }) {
//            val gattCallback = object : SimpleBluetoothGattCallback() {
//                override fun onCharacteristicChanged(
//                    gatt: BluetoothGatt,
//                    characteristic: BluetoothGattCharacteristic,
//                    value: ByteArray
//                ) {
//                    super.onCharacteristicChanged(gatt, characteristic, value)
//                    Timber.d("onCharacteristicChanged: ${characteristic.uuid} (gattCharacteristic.uuid is ${gattCharacteristic.uuid}) ${value.bytesToHex()}")
//
//                    if (characteristic.uuid == gattCharacteristic.uuid && value.isNotEmpty()) {
//                        val responseId = value[0].toInt() and 0xFF
//                        if (responseId == Command.CP_RESPONSE.value && value.size >= 4) {
//                            val originalCmd = value[1].toInt() and 0xFF
//                            val status = value[2].toInt() and 0xFF
//
//                            if (originalCmd == Command.SET_GNSS_MASK.value) {
//                                if (status == CP_STATUS_SUCCESS) {
//                                    val maskValue = value[3].toUByte()
//                                    resultDeferred.complete(maskValue)
//                                } else {
//                                    resultDeferred.completeExceptionally(
//                                        IllegalStateException("Get mask failed with status: $status")
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            gattTaskQueue += gattCharacteristic.uuid to gattCallback
//
//            val command = CommandBuilder.buildGetMaskCommand()
//            val task = GattTask.WriteTask(gatt, gattCharacteristic, command) {}
//
//            gattTaskQueue.addTask(task)
//            val returnValue = try {
//                if (timeout > 0L) {
//                    withTimeout(timeout) {
//                        resultDeferred.await()
//                    }
//                } else {
//                    resultDeferred.await()
//                }
//            } catch (e: TimeoutCancellationException) {
//                Timber.d(e.toString())
//                GnssMask.DEFAULT
//            } catch (e: Exception) {
//                Timber.e(e)
//                GnssMask.DEFAULT
//            }
//            gattTaskQueue -= gattCallback
//            returnValue
//        }
    }
}