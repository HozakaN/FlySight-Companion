package fr.hozakan.flysightcompanion.configfilesmodule.ui.list_files

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem

@Immutable
data class ListConfigFilesState(
    val configFiles: List<ConfigFile> = emptyList(),
    val unitSystem: UnitSystem = UnitSystem.Metric
)