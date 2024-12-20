package fr.hozakan.flysightble.fsdevicemodule.business.job

import fr.hozakan.flysightble.model.FileInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DirectoryFetcher {
    fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>>
    suspend fun listDirectory(directoryPath: List<String>): List<FileInfo>
}