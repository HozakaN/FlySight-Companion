package fr.hozakan.flysightcompanion.usbmodule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DefaultUsbService(
    context: Context,
    activityLifecycleService: ActivityLifecycleService
) : UsbService {

    private val _usbFlySights = MutableStateFlow<List<UsbDevice>>(emptyList())
    override val usbFlySights: StateFlow<List<UsbDevice>> = _usbFlySights.asStateFlow()

    private val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshDeviceList()
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        val attachedIntentFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        val detachedIntentFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        activityLifecycleService
            .appInForeground
            .onEach { isInForeground ->
                if (isInForeground) {
                    context.registerReceiver(receiver, attachedIntentFilter)
                    context.registerReceiver(receiver, detachedIntentFilter)
                    refreshDeviceList()
                } else {
                    context.unregisterReceiver(receiver)
                }
            }
            .launchIn(scope)
    }

    private fun refreshDeviceList() {
        manager.deviceList.filter { it.value.vendorId == VENDOR_ID && it.value.productId == PRODUCT_ID }
            .map { it.value }
            .also { _usbFlySights.value = it }
    }

}

private const val VENDOR_ID = 0x16d0
private const val PRODUCT_ID = 0x0569