package fr.hozakan.flysightble.fsdevicemodule.business.job

import fr.hozakan.flysightble.model.FileState

interface FileReader {
    suspend fun readFile(filePath: String): FileState
}