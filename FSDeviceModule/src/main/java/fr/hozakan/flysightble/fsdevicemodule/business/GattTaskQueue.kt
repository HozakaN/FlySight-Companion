package fr.hozakan.flysightble.fsdevicemodule.business

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

@SuppressLint("MissingPermission")
class GattTaskQueue(
    private val gattCallback: BluetoothGattCallback
) {

    private val tasks = mutableListOf<GattTask>()

    private val taskChannel = Channel<GattTask>(Channel.UNLIMITED)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun gattCallback() = _gattCallback

    private val _gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            gattCallback.onConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            gattCallback.onServicesDiscovered(gatt, status)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            gattCallback.onDescriptorWrite(gatt, descriptor, status)
            val task = tasks.filterIsInstance<GattTask.WriteDescriptorTask>().firstOrNull()
//            val task = tasks.firstOrNull { it.characteristic.uuid == characteristic.uuid && it.gatt == gatt }
            if (task != null) {
                task.completion.complete(Unit)
                tasks -= task
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            gattCallback.onCharacteristicRead(gatt, characteristic, value, status)
            val task = tasks.firstOrNull { it.characteristic.uuid == characteristic.uuid && it.gatt == gatt }
            if (task != null) {
                task.completion.complete(Unit)
                tasks -= task
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            gattCallback.onCharacteristicWrite(gatt, characteristic, status)
            val task = tasks.firstOrNull { it.characteristic.uuid == characteristic?.uuid && it.gatt == gatt }
            if (task != null) {
                task.completion.complete(Unit)
                tasks -= task
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            gattCallback.onCharacteristicChanged(gatt, characteristic, value)
        }
    }

    init {
        scope.launch {
            for (task in taskChannel) {
                Timber.d("Hoz picking task $task")
                processTask(task)
                Timber.d("Hoz task ended $task")
            }
        }
    }

    private suspend fun processTask(task: GattTask) {
        when (task) {
            is GattTask.ReadTask -> handleReadTask(task)
            is GattTask.WriteTask -> handleWriteTask(task)
            is GattTask.WriteDescriptorTask -> handleWriteDescriptorTask(task)
        }
    }

    private suspend fun handleReadTask(task: GattTask.ReadTask) {
        val gatt = task.gatt
        val characteristic = task.characteristic
        task.commandLogger()
        withTimeout(5000) {
            gatt.readCharacteristic(characteristic)
            task.completion.await()
        }
    }

    private suspend fun handleWriteTask(task: GattTask.WriteTask) {
        val gatt = task.gatt
        val characteristic = task.characteristic
        val command = task.command
        val writeType = task.writeType
        task.commandLogger()
        gatt.setCharacteristicNotification(characteristic, true)
        gatt.writeCharacteristic(
            characteristic,
            command,
            writeType
        )
        withTimeout(5000) {
            task.completion.await()
        }
        gatt.setCharacteristicNotification(characteristic, false)
    }

    private suspend fun handleWriteDescriptorTask(task: GattTask.WriteDescriptorTask) {
        val gatt = task.gatt
        val descriptor = task.descriptor
        val command = task.command
        task.commandLogger()
        gatt.writeDescriptor(descriptor, command)
        task.completion.await()
    }

    fun addTask(task: GattTask) {
        scope.launch {
            Timber.d("Hoz adding task $task")
            tasks += task
            taskChannel.send(task)
            Timber.d("Hoz task added $task")
        }
    }

}