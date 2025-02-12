package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.FileState

interface MutableFlySightDevice : FlySightDevice {
    suspend fun connect(): Boolean
    suspend fun disconnect(): Boolean
    suspend fun readFileSynchronously(fileName: String): FileState
    suspend fun updateConfigFile(configFile: ConfigFile)
    suspend fun writeBinaryFile(filePath: String, fileContent: ByteArray, callback: (Int) -> Unit): Boolean
}