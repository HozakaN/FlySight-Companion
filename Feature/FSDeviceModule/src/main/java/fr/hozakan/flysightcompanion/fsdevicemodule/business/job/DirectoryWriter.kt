package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

interface DirectoryWriter {
    suspend fun writeDirectory(filePath: String): Boolean
}