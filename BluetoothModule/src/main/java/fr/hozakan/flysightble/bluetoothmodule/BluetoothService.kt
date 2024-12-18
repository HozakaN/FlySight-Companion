package fr.hozakan.flysightble.bluetoothmodule

import android.bluetooth.BluetoothDevice
import fr.hozakan.flysightble.framework.service.loading.LoadingState
import kotlinx.coroutines.flow.Flow

interface BluetoothService {
    fun checkBluetoothState(): BluetoothState
    suspend fun enableBluetooth(): Boolean
    fun getPairedDevices(): Flow<LoadingState<List<BluetoothDevice>>>
    suspend fun awaitBluetoothAvailability()
    suspend fun addDevice()

    sealed interface BluetoothState {
        data object NotAvailable : BluetoothState
        data object NotEnabled : BluetoothState
        data object Available : BluetoothState
    }
}