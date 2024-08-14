package fr.hozakan.flysightble.bluetoothmodule

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import fr.hozakan.flysightble.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightble.framework.service.async.ActivityOperationsService
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resume

class DefaultBluetoothService(
    private val context: Context,
    private val activityOperationsService: ActivityOperationsService,
    private val activityLifecycleService: ActivityLifecycleService,
) : BluetoothService {

    private val bluetoothAdapter: BluetoothAdapter?

    private val btAvailabilityContinuations = mutableListOf<CancellableContinuation<Unit>>()

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
    override suspend fun getPairedDevices(): List<BluetoothDevice> {
        val adapter = bluetoothAdapter
        val devices = mutableListOf<BluetoothDevice>()
        if (adapter != null) {
            val bondedDevices = adapter.bondedDevices
            bondedDevices.onEachIndexed { index, device ->
                Timber.d("device $index : $device")
                if (isFlySightDevice(device)) {
                    devices.add(device)
                }
            }
        }
        return devices
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

    @SuppressLint("MissingPermission")
    private fun isFlySightDevice(device: BluetoothDevice): Boolean {
        return device.name?.startsWith("FlySight") ?: false
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(device: BluetoothDevice) {
        val uuid = UUID.randomUUID()
        val adapter = bluetoothAdapter ?: return
        val socket = try {
            device.createRfcommSocketToServiceRecord(uuid)
        } catch (ex: IOException)  {
            null
        }
    }
}