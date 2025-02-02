package fr.hozakan.flysightcompanion.usbmodule

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import fr.hozakan.flysightcompanion.model.Log
import kotlinx.coroutines.flow.StateFlow

interface UsbService {
    val hasFlySight: StateFlow<Boolean>
    val usbManager: UsbManager
    val usbFlySights: StateFlow<List<UsbDevice>>
    val usbLogs: StateFlow<List<Log>>
    suspend fun awaitUsbConnection()
    suspend fun awaitUsbDisconnection()
}