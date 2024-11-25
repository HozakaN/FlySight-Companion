package fr.hozakan.flysightble.configfilesmodule.business

import fr.hozakan.flysightble.model.ConfigFile
import kotlinx.coroutines.flow.StateFlow

interface ConfigFileService {
    val configFiles: StateFlow<List<ConfigFile>>
    suspend fun saveConfigFile(configFile: ConfigFile)
    suspend fun deleteConfigFile(configFile: ConfigFile)
}