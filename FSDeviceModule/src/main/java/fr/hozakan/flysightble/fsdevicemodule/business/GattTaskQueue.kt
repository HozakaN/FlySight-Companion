package fr.hozakan.flysightble.fsdevicemodule.business

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class GattTaskQueue(
    private val gattCallback: BluetoothGattCallback
) {

    private val tasks = mutableListOf<GattTask>()

    private val taskChannel = Channel<GattTask>(Channel.UNLIMITED)
    private val scope = CoroutineScope(
        SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    fun gattCallback() = _gattCallback

    private val characteristicChangeCallbacks = mutableMapOf<UUID, List<BluetoothGattCallback>>()

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
            val task =
                tasks.firstOrNull { it.characteristic.uuid == characteristic.uuid && it.gatt == gatt }
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
            Timber.d("Hoz2 onCharacteristicWrite characteristic=${characteristic?.uuid} status=$status")
            gattCallback.onCharacteristicWrite(gatt, characteristic, status)
            val task =
                tasks.firstOrNull { it.characteristic.uuid == characteristic?.uuid && it.gatt == gatt }
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
            characteristicChangeCallbacks[characteristic.uuid]?.let { callbacks ->
                callbacks.forEach { callback ->
                    callback.onCharacteristicChanged(gatt, characteristic, value)
                }
            }
        }
    }

    init {
        scope.launch {
            for (task in taskChannel) {
                Timber.d("Hoz2 picking task $task")
                processTask(task)
                Timber.d("Hoz2 task ended $task")
            }
        }
    }

    operator fun plusAssign(callback: Pair<UUID, BluetoothGattCallback>) {
        val list = characteristicChangeCallbacks[callback.first]
        if (list != null) {
            characteristicChangeCallbacks[callback.first] = list + callback.second
        } else {
            characteristicChangeCallbacks[callback.first] = listOf(callback.second)
        }
    }

    operator fun minusAssign(callback: BluetoothGattCallback) {
//        callbacks -= callback
        characteristicChangeCallbacks.forEach { (uuid, callbacks) ->
            characteristicChangeCallbacks[uuid] = callbacks.filter { it != callback }
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
        gatt.writeCharacteristic(
            characteristic,
            command,
            writeType
        )
        Timber.d("Hoz awaiting completion")
        withTimeout(5000) {
            task.completion.await()
            Timber.d("Hoz completion awaited before timeout")
        }
        Timber.d("Hoz completion awaited")
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