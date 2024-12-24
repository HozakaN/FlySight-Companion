package fr.hozakan.flysightcompanion.configfilesmodule.business

import fr.hozakan.flysightcompanion.model.ConfigFile
import kotlinx.coroutines.flow.StateFlow

interface ConfigFileService {
    val configFiles: StateFlow<List<ConfigFile>>
    suspend fun saveConfigFile(configFile: ConfigFile): ConfigFile
    suspend fun deleteConfigFile(configFile: ConfigFile)
    suspend fun updateConfigFile(oldConf: ConfigFile, newConf: ConfigFile)
    suspend fun userPickConfiguration(): ConfigFile?
}