package fr.hozakan.flysightble.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightble.bluetoothmodule.GattTask
import fr.hozakan.flysightble.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightble.framework.extension.bytesToHex
import fr.hozakan.flysightble.fsdevicemodule.business.job.FileReader
import fr.hozakan.flysightble.model.FileState
import fr.hozakan.flysightble.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber

class BleFileReader(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue
) : FileReader {

    private var fileData: ByteArray? = null
    private var fileDataPacketNumber: Int? = null
    private val fileContent = CompletableDeferred<FileState>()

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            val cmdCode = value[0].toInt() and 0xFF
            val cmd = Command.fromValue(cmdCode)
            if (cmd == Command.FILE_DATA) {
                handleFileDataPart(value.sliceArray(1 until value.size))
            }
        }
    }

    override suspend fun readFile(filePath: String): FileState {
        val task = TaskBuilder.buildReadFileTask(
            gatt = gatt,
            characteristic = gattCharacteristic,
            path = filePath,
            commandLogger = {}
        )
        gattTaskQueue += FlySightCharacteristic.CRS_TX.uuid to gattCallback
        gattTaskQueue.addTask(task)

        val fileState = fileContent.await()
        gattTaskQueue -= gattCallback
        return fileState
    }

    private fun handleFileDataPart(value: ByteArray) {
        val dataArray = value.sliceArray(1 until value.size)
        val packetId = value[0].toInt() and 0xFF
        if (fileData == null) {
            fileDataPacketNumber = packetId
            fileData = dataArray
            sendReadFileAck(packetId)
        } else {
            if (packetId == fileDataPacketNumber!! + 1) {
                fileDataPacketNumber = packetId
                sendReadFileAck(packetId)
                if (dataArray.isNotEmpty()) {
                    fileData = fileData!! + dataArray
                } else {
                    val fileState = FileState.Success(String(fileData!!, Charsets.UTF_8))
                    fileData = null
                    fileDataPacketNumber = null
                    fileContent.complete(fileState)
                }
            }
        }
    }

    private fun sendReadFileAck(packetId: Int) {
//        log("File reading ack on packet $packetId")

        val task = TaskBuilder.buildReadFileAckTask(
            gatt = gatt,
            characteristic = gattCharacteristic,
            packetId = packetId,
            commandLogger = {}
        )
        gattTaskQueue.addTask(task)
    }
}