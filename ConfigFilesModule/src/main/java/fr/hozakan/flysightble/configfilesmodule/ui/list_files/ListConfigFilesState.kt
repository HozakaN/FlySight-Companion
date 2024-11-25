package fr.hozakan.flysightble.configfilesmodule.ui.list_files

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.UnitSystem

@Immutable
data class ListConfigFilesState(
    val configFiles: List<ConfigFile> = emptyList(),
    val unitSystem: UnitSystem = UnitSystem.Metric
)