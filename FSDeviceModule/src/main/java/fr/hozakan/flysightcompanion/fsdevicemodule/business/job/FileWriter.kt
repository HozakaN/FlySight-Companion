package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

interface FileWriter {
    suspend fun writeFile(
        filePath: String,
        fileContent: String
    )
}