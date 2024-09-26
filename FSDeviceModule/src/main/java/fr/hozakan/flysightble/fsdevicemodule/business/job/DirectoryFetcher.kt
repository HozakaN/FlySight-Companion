package fr.hozakan.flysightble.fsdevicemodule.business.job

import fr.hozakan.flysightble.model.FileInfo
import kotlinx.coroutines.flow.StateFlow

interface DirectoryFetcher {
    fun listDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>>
    fun close()
}