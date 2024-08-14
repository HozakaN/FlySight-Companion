package fr.hozakan.flysightble.bluetoothmodule

import android.bluetooth.BluetoothDevice

interface BluetoothService {
    fun checkBluetoothState(): BluetoothState
    suspend fun enableBluetooth(): Boolean
    suspend fun getPairedDevices(): List<BluetoothDevice>
    suspend fun awaitBluetoothAvailability()
    suspend fun addDevice()
    suspend fun connect(device: BluetoothDevice)

    sealed interface BluetoothState {
        data object NotAvailable : BluetoothState
        data object NotEnabled : BluetoothState
        data object Available : BluetoothState
    }
}