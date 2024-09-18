package fr.hozakan.flysightble.configfilesmodule.ui.list_files

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightble.model.ConfigFile

@Immutable
data class ListConfigFilesState(
    val configFiles: List<ConfigFile> = emptyList()
)