package fr.hozakan.flysightcompanion.bluetoothmodule

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.framework.service.async.ActivityOperationsService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

class DefaultBluetoothService(
    private val context: Context,
    private val activityOperationsService: ActivityOperationsService
) : BluetoothService {

    private val bluetoothAdapter: BluetoothAdapter?

    private val btAvailabilityContinuations = mutableListOf<CancellableContinuation<Unit>>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
    }

    override fun checkBluetoothState(): BluetoothService.BluetoothState {
        val adapter = bluetoothAdapter
        return when {
            adapter == null -> BluetoothService.BluetoothState.NotAvailable
            !adapter.isEnabled -> BluetoothService.BluetoothState.NotEnabled
            else -> BluetoothService.BluetoothState.Available
        }
    }

    override suspend fun enableBluetooth(): Boolean {
        val state = checkBluetoothState()
        if (state == BluetoothService.BluetoothState.Available) {
            freeAwaitingBluetoothAvailabilityCoroutines()
            return true
        }
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        val result = activityOperationsService.requestActivityResult(enableBtIntent)
        if (result.first == Activity.RESULT_OK) {
            freeAwaitingBluetoothAvailabilityCoroutines()
            return true
        }
        return false
    }

    private fun freeAwaitingBluetoothAvailabilityCoroutines() {
        val continuations = ArrayList(btAvailabilityContinuations)
        btAvailabilityContinuations.clear()
        continuations.onEach { it.resume(Unit) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override fun discoverDevices(): Flow<LoadingState<List<BluetoothDevice>>> {
        return channelFlow {
            if (isClosedForSend) return@channelFlow
            send(LoadingState.Loading())

            val adapter = bluetoothAdapter
            val devices = mutableListOf<BluetoothDevice>()

            if (adapter != null) {
                val scanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        scope.launch {
                            result.scanRecord?.customAdvertisementDataMap()
                                ?.let { advertisingData ->
                                    val data2 = advertisingData[255] ?: return@let
                                    if (data2.bytesToHex().length != 8) return@let
                                    val manufacturerId = data2.bytesToHex().run {
                                        substring(2, length - 2)
                                    }
                                    if (manufacturerId == "DB09" && result.isConnectable/* && result.device.bondState == BluetoothDevice.BOND_BONDED*/) {
                                        if (result.device.address !in devices.map { it.address }) {
                                            devices += result.device
                                            if (!isClosedForSend) {
                                                send(LoadingState.Loading(devices))
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
                adapter.bluetoothLeScanner.startScan(scanCallback)
                delay(10_000)
                if (devices.isEmpty()) {
                    if (!isClosedForSend) {
                        send(LoadingState.Loading(currentLoad = devices, increment = 1))
                    }
                    delay(10_000)
                }
                if (devices.isEmpty()) {
                    if (!isClosedForSend) {
                        send(LoadingState.Loading(currentLoad = devices, increment = 2))
                    }
                    delay(20_000)
                }
                adapter.bluetoothLeScanner.stopScan(scanCallback)
                if (!isClosedForSend) {
                    send(LoadingState.Loaded(devices))
                }
            } else {
                if (!isClosedForSend) {
                    send(LoadingState.Error(IllegalStateException("Bluetooth adapter is null")))
                }
            }
        }
    }

    override suspend fun awaitBluetoothAvailability() {
        val brState = checkBluetoothState()
        if (brState == BluetoothService.BluetoothState.Available) {
            freeAwaitingBluetoothAvailabilityCoroutines()
            return
        }
        return suspendCancellableCoroutine { continuation ->
            btAvailabilityContinuations += continuation
            continuation.invokeOnCancellation {
                btAvailabilityContinuations -= continuation
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun addDevice(device: BluetoothDevice): Boolean {
        if (device.bondState == BluetoothDevice.BOND_BONDED) return true
        val deferred = CompletableDeferred<Boolean>()
        scope.launch {
            var gatt: BluetoothGatt? = null
            val bondStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                        val bondedDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        
                        if (bondedDevice?.address == device.address) {
                            when (bondState) {
                                BluetoothDevice.BOND_BONDED -> {
                                    context?.unregisterReceiver(this)
                                    deferred.complete(true)
                                    gatt?.disconnect()
                                    gatt?.close()
                                }
                                BluetoothDevice.BOND_NONE -> {
                                    context?.unregisterReceiver(this)
                                    deferred.complete(false)
                                    gatt?.disconnect()
                                    gatt?.close()
                                }
                                BluetoothDevice.BOND_BONDING -> {}
                            }
                        }
                    }
                }
            }
            
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(bondStateReceiver, filter)

            gatt = device.connectGatt(
                context,
                false,
                object : SimpleBluetoothGattCallback() {
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt?,
                        status: Int,
                        newState: Int
                    ) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gatt?.discoverServices()
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        device.createBond()
                    }
                }.asBluetoothGattCallback()
            )
        }
        return deferred.await()
    }

}

private fun ScanRecord.customAdvertisementDataMap(): Map<Int, ByteArray>? {
    return ScanRecordParser.parseFromBytes(this.bytes)
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        this.advertisingDataMap //[DATA_TYPE_MANUFACTURER_SPECIFIC_DATA]
//    } else {
//        parseFromBytes(this.bytes)
//    }
}