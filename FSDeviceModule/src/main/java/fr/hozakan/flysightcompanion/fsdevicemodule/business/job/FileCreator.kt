package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

interface FileCreator {
    suspend fun createFile(filePath: String)
}