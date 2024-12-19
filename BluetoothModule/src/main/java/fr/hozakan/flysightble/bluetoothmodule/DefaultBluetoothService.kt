package fr.hozakan.flysightble.bluetoothmodule

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.welie.blessed.BondState
import fr.hozakan.flysightble.framework.extension.bytesToHex
import fr.hozakan.flysightble.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightble.framework.service.async.ActivityOperationsService
import fr.hozakan.flysightble.framework.service.loading.LoadingState
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
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
    private val activityOperationsService: ActivityOperationsService,
    private val activityLifecycleService: ActivityLifecycleService,
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
        if (state == BluetoothService.BluetoothState.Available)  {
            freeAwaitingBluetoothAvailabilityCoroutines()
            return true
        }
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        val result = activityOperationsService.requestActivityResult(enableBtIntent)
        if (result.first == Activity.RESULT_OK)  {
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

    @SuppressLint("MissingPermission")
    override fun getPairedDevices(): Flow<LoadingState<List<BluetoothDevice>>> {
        return channelFlow {
            send(LoadingState.Loading())
            val adapter = bluetoothAdapter
            val devices = mutableListOf<BluetoothDevice>()

            if (adapter != null) {
                val scanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        result.scanRecord?.advertisingDataMap?.let { advertisingData ->
                            val data2 = advertisingData[255] ?: return@let
                            if (data2.bytesToHex().length != 8) return@let
                            val manufacturerId = data2.bytesToHex().run {
                                substring(2, length - 2)
                            }
                            if (manufacturerId == "DB09" && result.isConnectable && result.device.bondState == BondState.BONDED.value) {
                                devices += result.device
                                scope.launch {
                                    send(LoadingState.Loading(devices))
                                }
                            }
                        }
                    }
                }
                adapter.bluetoothLeScanner.startScan(scanCallback)
                delay(10_000)
                if (devices.isEmpty()) {
                    send(LoadingState.Loading(currentLoad = devices, increment = 1))
                    delay(10_000)
                }
                if (devices.isEmpty()) {
                    send(LoadingState.Loading(currentLoad = devices, increment = 2))
                    delay(20_000)
                }
                adapter.bluetoothLeScanner.stopScan(scanCallback)
                send(LoadingState.Loaded(devices))
            } else {
                send(LoadingState.Error(IllegalStateException("Bluetooth adapter is null")))
            }
        }
    }

    override suspend fun awaitBluetoothAvailability() {
        val brState = checkBluetoothState()
        if (brState == BluetoothService.BluetoothState.Available)  {
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

    override suspend fun addDevice() {
        try {
            val intentOpenBluetoothSettings = Intent()
            intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
            intentOpenBluetoothSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intentOpenBluetoothSettings)
            activityLifecycleService.awaitNextResume()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}