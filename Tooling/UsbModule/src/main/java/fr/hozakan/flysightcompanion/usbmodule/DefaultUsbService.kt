package fr.hozakan.flysightcompanion.usbmodule

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.model.Log
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

class DefaultUsbService(
    private val context: Context,
    activityLifecycleService: ActivityLifecycleService
) : UsbService {

    private val _usbFlySights = MutableStateFlow<List<UsbDevice>>(emptyList())
    override val usbFlySights: StateFlow<List<UsbDevice>> = _usbFlySights.asStateFlow()

    private val _usbLogs = MutableStateFlow<List<Log>>(emptyList())
    override val usbLogs: StateFlow<List<Log>> = _usbLogs.asStateFlow()

    override val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _hasFlySight = MutableStateFlow(false)
    override val hasFlySight: StateFlow<Boolean> = _hasFlySight.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                freeConnectionContinuations()
                _hasFlySight.value = true
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                freeDisconnectionContinuations()
                _hasFlySight.value = false
            }
//            refreshDeviceList()
        }
    }

    private var usbContinuation: CancellableContinuation<Boolean>? = null
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            synchronized(this) {

                val hasPermission =
                    intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                context.unregisterReceiver(this)
                usbContinuation?.resume(hasPermission)
                usbContinuation = null
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var usbConnectionContinuations = mutableListOf<CancellableContinuation<Unit>>()
    private var usbDisconnectionContinuations = mutableListOf<CancellableContinuation<Unit>>()

    init {
        val attachedIntentFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        val detachedIntentFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        activityLifecycleService
            .appInForeground
            .onEach { isInForeground ->
                if (isInForeground) {
                    context.registerReceiver(receiver, attachedIntentFilter)
                    context.registerReceiver(receiver, detachedIntentFilter)
//                    refreshDeviceList()
                } else {
                    try {
                        context.unregisterReceiver(receiver)
                    } catch (_: IllegalArgumentException) {
                    }
                }
            }
            .launchIn(scope)
    }

    override suspend fun awaitUsbConnection() {
        if (!_hasFlySight.value) suspendCancellableCoroutine<Unit> { continuation ->
            usbConnectionContinuations += continuation
            continuation.invokeOnCancellation {
                usbConnectionContinuations -= continuation
            }
        }
    }

    override suspend fun awaitUsbDisconnection() {
        if (_hasFlySight.value) {
            suspendCancellableCoroutine<Unit> { continuation ->
                usbDisconnectionContinuations += continuation
                continuation.invokeOnCancellation {
                    usbDisconnectionContinuations -= continuation
                }
            }
        }
    }

    private fun freeConnectionContinuations() {
        val continuations = ArrayList(usbConnectionContinuations)
        usbConnectionContinuations.clear()
        continuations.forEach {
            if (it.isActive) {
                it.resume(Unit)
            }
        }
    }

    private fun freeDisconnectionContinuations() {
        val continuations = ArrayList(usbDisconnectionContinuations)
        usbDisconnectionContinuations.clear()
        continuations.forEach {
            if (it.isActive) {
                it.resume(Unit)
            }
        }
    }

    private fun refreshDeviceList() {
        scope.launch {
            log("Refreshing device list")
            log("_______________________________________________")
            usbManager.deviceList.filter { it.value.vendorId == VENDOR_ID && it.value.productId == PRODUCT_ID }
                .map {
                    log("device found : ${it.key}, ${it.value.deviceName}")
                    it.value
                }
                .mapIndexedNotNull { index, usbDevice ->
                    val hasPermission = usbManager.hasPermission(usbDevice)
                    log("checking permission for device $index : $hasPermission")
                    if (hasPermission) {
                        usbDevice
                    } else {
                        if (requestPermission(usbDevice)) {
                            log("permission granted for device $index")
                            usbDevice
                        } else {
                            log("permission NOT granted for device $index")
                            null
                        }
                    }
                }
                .also {
                    log("setting available device list to ${it.size} elements")
                    _usbFlySights.value = it
                }
            log("_______________________________________________")
        }
    }

    private suspend fun requestPermission(device: UsbDevice): Boolean =
        suspendCancellableCoroutine { continuation ->
            usbContinuation = continuation
            val permissionIntent = PendingIntent.getBroadcast(
                context, 0,
                Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            ContextCompat.registerReceiver(
                context,
                usbReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            usbManager.requestPermission(device, permissionIntent)
            continuation.invokeOnCancellation {
                usbContinuation = null
            }
        }

    private fun log(message: String) {
        //Add to logs and add time value as a prefix
        _usbLogs.value += Log(message = message)
    }

}

private const val VENDOR_ID = 0x16d0
private const val PRODUCT_ID = 0x0569

private const val ACTION_USB_PERMISSION = "fr.hozakan.flysightcompanion.USB_PERMISSION"