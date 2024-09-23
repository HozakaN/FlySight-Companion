package fr.hozakan.flysightble.fsdevicemodule.business.job

interface FileWriter {
    suspend fun writeFile(
        filePath: String,
        fileContent: String
    )
}