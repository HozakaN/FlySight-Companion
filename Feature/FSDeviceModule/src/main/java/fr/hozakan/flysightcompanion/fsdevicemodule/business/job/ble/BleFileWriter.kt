package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FileWriter
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber

class BleFileWriter(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : FileWriter {

    private val writeAck = CompletableDeferred<Unit>()
    private val fileDataSent = CompletableDeferred<Unit>()
    private val dataPackets = mutableListOf<ByteArray>()
    private var currentPacket = 0

    override suspend fun writeFile(
        filePath: String,
        fileContent: String
    ) {
        writeFile(filePath, fileContent.toByteArray(), {})
    }

    override suspend fun writeFile(
        filePath: String,
        fileContent: ByteArray,
        callback: (Int) -> Unit
    ) {
        scheduler.schedule(
            labelProvider = { "Write file $filePath" }
        ) {
            val gattCallback = object : SimpleBluetoothGattCallback() {
                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    super.onCharacteristicChanged(gatt, characteristic, value)
                    Timber.d("Hoz3 reading ${value.bytesToHex()}")
                    val cmdCode = value[0].toInt() and 0xFF
                    val cmd = Command.fromValue(cmdCode)
                    if (cmd == Command.ACK) {
                        val cmdAckedCode = value[1].toInt() and 0xFF
                        val cmdAcked = Command.fromValue(cmdAckedCode)
                        if (cmdAcked == Command.WRITE) {
                            writeAck.complete(Unit)
                        }
                    } else if (cmd == Command.NAK) {
                        val cmdAckedCode = value[1].toInt() and 0xFF
                        val cmdAcked = Command.fromValue(cmdAckedCode)
                        if (cmdAcked == Command.WRITE) {
                            writeAck.completeExceptionally(Exception("NAK received"))
                        }
                    } else if (cmd == Command.FILE_ACK) {
                        val ackNum = value[1].toInt() and 0xFF
                        if (ackNum == currentPacket.mod(256)) {
                            currentPacket++
                            if (currentPacket > dataPackets.size) {
                                fileDataSent.complete(Unit)
                            } else {
                                callback(currentPacket * FRAME_LENGTH)
                                sendNextDataPacket()
                            }
                        }
                    }
                }
            }

            gattTaskQueue += FlySightCharacteristic.CRS_TX.uuid to gattCallback
            val writeTask = TaskBuilder.buildWriteFileTask(
                gatt,
                gattCharacteristic,
                filePath
            ) {}
            gattTaskQueue.addTask(writeTask)
            try {
                writeAck.await()
            } catch (e: Exception) {
                gattTaskQueue -= gattCallback
                throw e
            }
            prepareDataPackets(fileContent)
            Timber.d("Hoz3 sending first ping packet")
            sendNextDataPacket()
//            sendPingPacket()
            try {
                Timber.d("Hoz3 awaiting file sent")
                fileDataSent.await()
            } catch (e: Exception) {
                gattTaskQueue -= gattCallback
                throw e
            }
            gattTaskQueue -= gattCallback
        }
    }

    private fun prepareDataPackets(fileContent: ByteArray) {
        val nbPackets = fileContent.size / FRAME_LENGTH + 1
        for (i in 0 until nbPackets) {
            val packet = try {
                fileContent.copyOfRange(i * FRAME_LENGTH, (i + 1) * FRAME_LENGTH)
            } catch (e: Exception) {
                fileContent.copyOfRange(i * FRAME_LENGTH, fileContent.size)
            }
            dataPackets.add(packet)
        }
    }

    private fun sendNextDataPacket() {
        val writeTask = if (currentPacket >= dataPackets.size) {
            TaskBuilder.buildWriteFileDataTask(
                gatt,
                gattCharacteristic,
                currentPacket,
                byteArrayOf()
            ) {}
        } else {
            val chunk = dataPackets[currentPacket]
            TaskBuilder.buildWriteFileDataTask(
                gatt,
                gattCharacteristic,
                currentPacket.mod(256),
                chunk
            ) {}
        }
        gattTaskQueue.addTask(writeTask)
    }
}

private const val FRAME_LENGTH = 242