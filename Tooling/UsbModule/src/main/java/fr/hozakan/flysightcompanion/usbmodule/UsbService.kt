package fr.hozakan.flysightcompanion.usbmodule

import android.hardware.usb.UsbDevice
import kotlinx.coroutines.flow.StateFlow

interface UsbService {
    val usbFlySights: StateFlow<List<UsbDevice>>
}