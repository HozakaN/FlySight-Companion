package fr.hozakan.flysightcompanion.sessionmodule.business

import android.content.Context
import com.google.gson.Gson
import fr.hozakan.flysightcompanion.dialogmodule.ConfigFileName
import fr.hozakan.flysightcompanion.dialogmodule.ConfigFileNameDialog
import fr.hozakan.flysightcompanion.dialogmodule.DialogResult
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.PickConfigurationDialog
import fr.hozakan.flysightcompanion.dialogmodule.PickConfigurationDialogResult
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DefaultSessionProfilesService(
    private val context: Context,
    private val dialogService: DialogService
) : SessionProfilesService {

    private val serviceScope = CoroutineScope(SupervisorJob())

    private val gson = Gson()

    private val _sessionConfigurations = MutableStateFlow<List<SessionProfile>>(emptyList())
    override val sessionProfiles: StateFlow<List<SessionProfile>> =
        _sessionConfigurations.asStateFlow()

    init {
        serviceScope.launch {
            loadSessionConfigurations()
        }
    }

    override suspend fun saveProfile(sessionProfile: SessionProfile): SessionProfile {
        var name = sessionProfile.name
        if (name.isBlank()) {
            when (val result = dialogService.displayDialog(ConfigFileNameDialog())) {
                is ConfigFileName -> name = result.name
                DialogResult.Dismiss -> return sessionProfile
                else -> error("Save session config result should not have another type (${result::class.java})")
            }
        }
        val readyConfigFile = sessionProfile.copy(name = name)
        _sessionConfigurations.update {
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

    override suspend fun updateProfile(oldConf: SessionProfile, newConf: SessionProfile) {
        _sessionConfigurations.update { configs ->
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

    override suspend fun deleteProfile(sessionProfile: SessionProfile) {
        val file =
            File("${getOrCreateConfigFilesFolder().absolutePath}${File.separator}${sessionProfile.name}.txt")
        file.delete()
        _sessionConfigurations.update {
            it - sessionProfile
        }
    }

    override suspend fun userPickProfile(): SessionProfile? {
        val configs = _sessionConfigurations.value
        if (configs.isEmpty()) return null
        val dialogItem = PickConfigurationDialog {
            configs
        }
        return when (val result = dialogService.displayDialog(dialogItem)) {
            is PickConfigurationDialogResult -> {
                result.configFile as? SessionProfile
            }

            DialogResult.Dismiss -> null
            else -> error("Pick config file result should not have another type")
        }
    }

    override suspend fun duplicateProfile(sessionProfile: SessionProfile) {
        var index = 1
        var name = "${sessionProfile.name} ($index)"
        while (_sessionConfigurations.value.any { it.name == name }) {
            index++
            name = "${sessionProfile.name} ($index)"
        }
        when (val result = dialogService.displayDialog(ConfigFileNameDialog(name))) {
            is ConfigFileName -> name = result.name
            DialogResult.Dismiss -> return
            else -> error("Duplicate session config should not have another output")
        }
        saveProfile(
            sessionProfile.copy(
                name = name
            )
        )
    }

    private fun getOrCreateConfigFilesFolder(): File {
        val folder =
            File("${context.filesDir.absolutePath}${File.separator}$SESSION_CONFIGS_FOLDER")
        val success = folder.exists() || folder.mkdir()
        return if (success) folder else throw IllegalAccessException("Cannot access app folder")
    }

    private suspend fun loadSessionConfigurations() {
        withContext(Dispatchers.IO) {
            val configFolder = getOrCreateConfigFilesFolder()
            val sessionConfigurations =
                (configFolder.listFiles()?.mapNotNull { parseConfiguration(it.readLines()) }
                    ?: emptyList())
            _sessionConfigurations.update {
                sessionConfigurations
            }
        }
    }

    private fun buildFileContent(sessionProfile: SessionProfile): String {
        return gson.toJson(sessionProfile)
    }

    private fun parseConfiguration(fileLines: List<String>): SessionProfile = gson.fromJson(fileLines.joinToString(separator = "\n"), SessionProfile::class.java)

    companion object {
        private const val SESSION_CONFIGS_FOLDER = "sessionConfigurations"
    }
}