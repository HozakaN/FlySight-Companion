package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.hardware.usb.UsbDevice

interface UsbFlySightDeviceDelegate : FlySightDeviceDelegate {
    val usbDevice: UsbDevice
}