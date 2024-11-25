package fr.hozakan.flysightble.configfilesmodule.business

import android.content.Context
import fr.hozakan.flysightble.configfilesmodule.business.parser.CONFIG_FILES_FOLDER
import fr.hozakan.flysightble.model.ConfigFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DefaultConfigFileService(
    private val context: Context
) : ConfigFileService {

    private val configSharedPreferences =
        context.getSharedPreferences("config_files", Context.MODE_PRIVATE)

    private val _configs = MutableStateFlow<List<ConfigFile>>(emptyList())
    override val configFiles = _configs.asStateFlow()

    private val serviceScope = CoroutineScope(SupervisorJob())

    private val parser: ConfigParser = DefaultConfigParser()

    private val encoder: ConfigEncoder = DefaultConfigEncoder()

    init {
        serviceScope.launch {
            loadConfigFiles()
        }
    }

    override suspend fun saveConfigFile(configFile: ConfigFile) {
        _configs.update {
            it + configFile
        }
        val fileContent = withContext(Dispatchers.IO) {
            buildFileContent(configFile)
        }
        val file =
            File("${getOrCreateConfigFilesFolder().absolutePath}${File.separator}${configFile.name}.txt")
        file.writeText(fileContent)
    }

    override suspend fun deleteConfigFile(configFile: ConfigFile) {
        val file =
            File("${getOrCreateConfigFilesFolder().absolutePath}${File.separator}${configFile.name}.txt")
        file.delete()
        _configs.update {
            it - configFile
        }
    }

    private fun buildFileContent(configFile: ConfigFile): String {
        return encoder.encodeConfig(configFile)
    }

    private fun getOrCreateConfigFilesFolder(): File {
        val folder =
            File("${context.filesDir.absolutePath}${File.separator}$CONFIG_FILES_FOLDER")
        val success = folder.exists() || folder.mkdir()
        return if (success) folder else throw IllegalAccessException("Cannot access app folder")
    }

    private suspend fun loadConfigFiles() {
        withContext(Dispatchers.IO) {
            val configFolder = getOrCreateConfigFilesFolder()
            val configFiles =
                (configFolder.listFiles()?.mapNotNull { parseConfiguration(it.readLines()) }
                    ?: emptyList()).toMutableList()
            _configs.update {
                configFiles
            }
        }
    }

    //    private fun parseConfiguration(configFile: File): ConfigFile? {
    private fun parseConfiguration(fileLines: List<String>): ConfigFile? {
        val configFile = parser.parse(fileLines)
        return configFile
    }

}



private val configItemsEncoders = listOf(
    "Model",
)