package fr.hozakan.flysightcompanion.fsdevicemodule.business

import kotlinx.coroutines.flow.SharedFlow

interface BleFlySightDeviceDelegate : FlySightDeviceDelegate {
    val address: String
    val ping: SharedFlow<Boolean>
//    suspend fun connectGatt(): Boolean
//    suspend fun disconnectGatt(): Boolean

    suspend fun startGNSSFeed()
    suspend fun stopGNSSFeed()
}