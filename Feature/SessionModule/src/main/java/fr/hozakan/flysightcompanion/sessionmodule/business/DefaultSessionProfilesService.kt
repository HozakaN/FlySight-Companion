package fr.hozakan.flysightcompanion.sessionmodule.business

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import fr.hozakan.flysightcompanion.dialogmodule.ConfigFileNameDialog
import fr.hozakan.flysightcompanion.dialogmodule.ConfigFileNameDialogResult
import fr.hozakan.flysightcompanion.dialogmodule.DialogResult
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.PickConfigurationDialog
import fr.hozakan.flysightcompanion.dialogmodule.PickConfigurationDialogResult
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItemBundle
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint
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
import java.lang.reflect.Type

class DefaultSessionProfilesService(
    private val context: Context,
    private val dialogService: DialogService
) : SessionProfilesService {

    private val serviceScope = CoroutineScope(SupervisorJob())

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(DisplayItemBundle::class.java, DisplayItemBundleTypeAdapter())
        .create()

    private val _sessionProfiles = MutableStateFlow<List<SessionProfile>>(emptyList())
    override val sessionProfiles: StateFlow<List<SessionProfile>> =
        _sessionProfiles.asStateFlow()

    init {
        serviceScope.launch {
            loadSessionProfiles()
        }
    }

    // Type adapter for DisplayItemBundle
    private class DisplayItemBundleTypeAdapter : 
            JsonSerializer<DisplayItemBundle>, 
            JsonDeserializer<DisplayItemBundle> {
            
        override fun serialize(
            src: DisplayItemBundle, 
            typeOfSrc: Type, 
            context: JsonSerializationContext
        ): JsonElement {
            val jsonObject = JsonObject()
            
            // Add a type field to identify which subclass it is
            when (src) {
                is DisplayItemBundle.DistanceToRefPointBundle -> {
                    jsonObject.addProperty("type", "DistanceToRefPointBundle")
                    // Serialize the reference point
                    val refPoint = src.referencePoint
                    jsonObject.add("referencePoint", context.serialize(refPoint))
                }
            }
            
            return jsonObject
        }

        override fun deserialize(
            json: JsonElement, 
            typeOfT: Type, 
            context: JsonDeserializationContext
        ): DisplayItemBundle {
            val jsonObject = json.asJsonObject
            val type = jsonObject.get("type").asString
            
            return when (type) {
                "DistanceToRefPointBundle" -> {
                    val refPoint = context.deserialize<ReferencePoint>(
                        jsonObject.get("referencePoint"), 
                        ReferencePoint::class.java
                    )
                    DisplayItemBundle.DistanceToRefPointBundle(refPoint)
                }
                else -> throw JsonParseException("Unknown DisplayItemBundle type: $type")
            }
        }
    }

    override suspend fun saveProfile(sessionProfile: SessionProfile): SessionProfile {
        var name = sessionProfile.name
        if (name.isBlank()) {
            when (val result = dialogService.displayDialog(ConfigFileNameDialog())) {
                is ConfigFileNameDialogResult -> name = result.name
                DialogResult.Dismiss -> return sessionProfile
                else -> error("Save session config result should not have another type (${result::class.java})")
            }
        }
        val readyConfigFile = sessionProfile.copy(name = name)
        _sessionProfiles.update {
            it + readyConfigFile
        }
        val fileContent = withContext(Dispatchers.IO) {
            buildFileContent(readyConfigFile)
        }
        val file =
            File("${getOrCreateProfilesFolder().absolutePath}${File.separator}${readyConfigFile.name}.TXT")
        file.writeText(fileContent)
        return readyConfigFile
    }

    override suspend fun updateProfile(oldConf: SessionProfile, newConf: SessionProfile) {
        _sessionProfiles.update { configs ->
            val index = configs.indexOfFirst { it.name == oldConf.name }
            val profiles = (configs - configs.first { it.name == oldConf.name }).run {
                toMutableList().also { mutableList -> mutableList.add(index, newConf) }
            }
            profiles
        }
        val fileContent = withContext(Dispatchers.IO) {
            buildFileContent(newConf)
        }
        val file =
            File("${getOrCreateProfilesFolder().absolutePath}${File.separator}${newConf.name}.TXT")
        file.writeText(fileContent)

        if (oldConf.name != newConf.name) {
            val oldFile =
                File("${getOrCreateProfilesFolder().absolutePath}${File.separator}${oldConf.name}.TXT")
            oldFile.delete()
        }
    }

    override suspend fun deleteProfile(sessionProfile: SessionProfile) {
        val file =
            File("${getOrCreateProfilesFolder().absolutePath}${File.separator}${sessionProfile.name}.TXT")
        file.delete()
        _sessionProfiles.update {
            it - sessionProfile
        }
    }

    override suspend fun userPickProfile(): SessionProfile? {
        val configs = _sessionProfiles.value
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
        while (_sessionProfiles.value.any { it.name == name }) {
            index++
            name = "${sessionProfile.name} ($index)"
        }
        when (val result = dialogService.displayDialog(ConfigFileNameDialog(name))) {
            is ConfigFileNameDialogResult -> name = result.name
            DialogResult.Dismiss -> return
            else -> error("Duplicate session config should not have another output")
        }
        saveProfile(
            sessionProfile.copy(
                name = name
            )
        )
    }

    private suspend fun getOrCreateProfilesFolder(): File {
        val folder =
            File("${context.filesDir.absolutePath}${File.separator}$SESSION_PROFILES_FOLDER")
        val success = folder.exists() || (folder.mkdir() && addDefaultConfigs())
        return if (success) folder else throw IllegalAccessException("Cannot access app folder")
    }

    private suspend fun addDefaultConfigs(): Boolean {
        withContext(Dispatchers.IO) {
            val beaufortSession =
                javaClass.classLoader.getResource("Beaufort temps North-south 4.TXT")?.readText() ?: ""
            val beaufortSession2 = buildFileContent(SessionProfile.default())

//            val profileJson = parseConfiguration(beaufortSession.lines())
//            val fileContent =
//                buildFileContent(profileJson)
            val file =
                File("${context.filesDir.absolutePath}${File.separator}$SESSION_PROFILES_FOLDER${File.separator}Beaufort temps North-south 4.TXT")
            file.writeText(beaufortSession)
        }
        return true
    }

    private suspend fun loadSessionProfiles() {
        withContext(Dispatchers.IO) {
            val profilesFolder = getOrCreateProfilesFolder()
            val sessionProfiles =
                (profilesFolder.listFiles()?.mapNotNull { parseProfile(it.readLines()) }
                    ?: emptyList())
            _sessionProfiles.update {
                sessionProfiles
            }
        }
    }

    private fun buildFileContent(sessionProfile: SessionProfile): String {
        return gson.toJson(sessionProfile)
    }

    private fun parseProfile(fileLines: List<String>): SessionProfile =
        gson.fromJson(fileLines.joinToString(separator = "\n"), SessionProfile::class.java)/*.copy(
            displayFlareDetector = true,
            showMap = false
        )*/

    companion object {
        private const val SESSION_PROFILES_FOLDER = "session_profiles"
    }
}
