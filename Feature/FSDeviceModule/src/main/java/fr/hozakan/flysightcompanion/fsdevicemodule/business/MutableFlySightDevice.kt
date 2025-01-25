package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.FileState

interface MutableFlySightDevice : FlySightDevice {
    suspend fun connectGatt(): Boolean
    suspend fun disconnectGatt(): Boolean
    suspend fun readFileSynchronously(fileName: String): FileState
    suspend fun updateConfigFile(configFile: ConfigFile)
}