package fr.hozakan.flysightcompanion.recordsmodule.ui.plot

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.ui.PlotDisplayPreference

@Immutable
data class PlotSettingsState(
    val plotDisplayPreferences: List<PlotDisplayPreference> = PlotDisplayPreference.defaultValues()
)