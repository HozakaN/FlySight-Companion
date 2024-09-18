package fr.hozakan.flysightble.configfilesmodule.business

import android.content.Context
import fr.hozakan.flysightble.configfilesmodule.business.parser.CONFIG_FILES_FOLDER
import fr.hozakan.flysightble.configfilesmodule.business.parser.ConfigItemParser
import fr.hozakan.flysightble.configfilesmodule.business.ConfigParser
import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.DynamicModelParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.FILENAME_INDICATOR
import fr.hozakan.flysightble.configfilesmodule.business.parser.MultilineConfigItemParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.SpeechParser
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile
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

    private fun buildFileContent(configFile: ConfigFile): String {
        return buildString {
            append("; $FILENAME_INDICATOR${configFile.name}\n")
            configItemsEncoders.forEach { key ->
//                append("$key=${configFile.data[key]}\n")
            }
        }
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