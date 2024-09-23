package fr.hozakan.flysightble.fsdevicemodule.business.job

interface FileCreator {
    suspend fun createFile(filePath: String)
}