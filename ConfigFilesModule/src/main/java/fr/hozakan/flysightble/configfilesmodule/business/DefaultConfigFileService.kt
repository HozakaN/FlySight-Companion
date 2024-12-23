package fr.hozakan.flysightble.configfilesmodule.business

import android.content.Context
import fr.hozakan.flysightble.dialogmodule.ConfigFileName
import fr.hozakan.flysightble.dialogmodule.ConfigFileNameDialog
import fr.hozakan.flysightble.dialogmodule.DialogResult
import fr.hozakan.flysightble.dialogmodule.DialogService
import fr.hozakan.flysightble.dialogmodule.PickConfigurationDialog
import fr.hozakan.flysightble.dialogmodule.PickConfigurationDialogResult
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
    private val context: Context,
    private val dialogService: DialogService,
    private val configEncoder: ConfigEncoder
) : ConfigFileService {

    private val _configs = MutableStateFlow<List<ConfigFile>>(emptyList())
    override val configFiles = _configs.asStateFlow()

    private val serviceScope = CoroutineScope(SupervisorJob())

    private val parser: ConfigParser = DefaultConfigParser()

    init {
        serviceScope.launch {
            loadConfigFiles()
        }
    }

    override suspend fun saveConfigFile(configFile: ConfigFile): ConfigFile {
        var name = configFile.name
        if (name.isBlank()) {
            when (val result = dialogService.displayDialog(ConfigFileNameDialog)) {
                is ConfigFileName -> name = result.name
                DialogResult.Dismiss -> return configFile
                else -> error("Save config file result should not have another type")
            }
        }
        val readyConfigFile = configFile.copy(name = name)
        _configs.update {
            it + readyConfigFile
        }
        val fileContent = withContext(Dispatchers.IO) {
            buildFileContent(readyConfigFile)
        }
        val file =
            File("${getOrCreateConfigFilesFolder().absolutePath}${File.separator}${readyConfigFile.name}.txt")
        file.writeText(fileContent)
        return readyConfigFile
    }

    override suspend fun updateConfigFile(oldConf: ConfigFile, newConf: ConfigFile) {
        _configs.update { configs ->
            val index = configs.indexOfFirst { it.name == oldConf.name }
            (configs - configs.first { it.name == oldConf.name }).run {
                toMutableList().also { mutableList -> mutableList.add(index, newConf) }
            }
        }
        val fileContent = withContext(Dispatchers.IO) {
            buildFileContent(newConf)
        }
        val file =
            File("${getOrCreateConfigFilesFolder().absolutePath}${File.separator}${newConf.name}.txt")
        file.writeText(fileContent)

        if (oldConf.name != newConf.name) {
            val oldFile =
                File("${getOrCreateConfigFilesFolder().absolutePath}${File.separator}${oldConf.name}.txt")
            oldFile.delete()
        }
    }

    override suspend fun deleteConfigFile(configFile: ConfigFile) {
        val file =
            File("${getOrCreateConfigFilesFolder().absolutePath}${File.separator}${configFile.name}.txt")
        file.delete()
        _configs.update {
            it - configFile
        }
    }

    override suspend fun userPickConfiguration(): ConfigFile? {
        val configs = _configs.value
        if (configs.isEmpty()) return null
        return when (val result = dialogService.displayDialog(PickConfigurationDialog {
            configs
        })) {
            is PickConfigurationDialogResult -> {
                result.configFile
            }

            DialogResult.Dismiss -> null
            else -> error("Pick config file result should not have another type")
        }
    }

    private fun buildFileContent(configFile: ConfigFile): String {
        return configEncoder.encodeConfig(configFile)
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

    private fun parseConfiguration(fileLines: List<String>): ConfigFile = parser.parse(fileLines)

}