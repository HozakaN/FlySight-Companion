package fr.hozakan.flysightble.bluetoothmodule

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import timber.log.Timber

abstract class SimpleBluetoothGattCallback {

    open fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {}

    open fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {}

    open fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {}

    open fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {}

    open fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
    }

    open fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
    }

    open fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
    }

    open fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
        value: ByteArray
    ) {
    }

    open fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
    }

    open fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {}

    open fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {}

    open fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {}

    open fun onServiceChanged(gatt: BluetoothGatt) {}

    inner class AndroidVersionSafeBluetoothGattCallback(
        private val scope: SimpleBluetoothGattCallback
    ) : BluetoothGattCallback() {
        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            Timber.d("Hoz2 onPhyUpdate")
            scope.onPhyUpdate(gatt, txPhy, rxPhy, status)
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            Timber.d("Hoz2 onPhyRead")
            scope.onPhyRead(gatt, txPhy, rxPhy, status)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Timber.d("Hoz2 onConnectionStateChange")
            scope.onConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Timber.d("Hoz2 onServicesDiscovered")
            scope.onServicesDiscovered(gatt, status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Timber.d("Hoz2 onCharacteristicRead 3")
            gatt ?: return
            characteristic ?: return
            val value = characteristic.value ?: return
            scope.onCharacteristicRead(gatt, characteristic, value, status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            Timber.d("Hoz2 onCharacteristicRead 4")
            scope.onCharacteristicRead(gatt, characteristic, value, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Timber.d("Hoz2 onCharacteristicWrite")
            scope.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Timber.d("Hoz2 onCharacteristicChanged 2")
            gatt ?: return
            characteristic ?: return
            val value = characteristic.value ?: return
            scope.onCharacteristicChanged(gatt, characteristic, value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Timber.d("Hoz2 onCharacteristicChanged 3")
            scope.onCharacteristicChanged(gatt, characteristic, value)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            Timber.d("Hoz2 onDescriptorRead 3")
            gatt ?: return
            descriptor ?: return
            val value = descriptor.value ?: return
            scope.onDescriptorRead(gatt, descriptor, status, value)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            Timber.d("Hoz2 onDescriptorRead 4")
            scope.onDescriptorRead(gatt, descriptor, status, value)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            scope.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            scope.onReliableWriteCompleted(gatt, status)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            scope.onReadRemoteRssi(gatt, rssi, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            scope.onMtuChanged(gatt, mtu, status)
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            scope.onServiceChanged(gatt)
        }
    }

}

fun SimpleBluetoothGattCallback.asBluetoothGattCallback(): BluetoothGattCallback =
    AndroidVersionSafeBluetoothGattCallback(scope = this)