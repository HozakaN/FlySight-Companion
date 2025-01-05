package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

import fr.hozakan.flysightcompanion.model.FileState

interface FileReader {
    suspend fun readFile(filePath: String): FileState
}