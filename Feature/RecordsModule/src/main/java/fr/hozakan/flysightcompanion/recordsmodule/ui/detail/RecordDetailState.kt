package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.records.RecordAnalyze
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.model.records.dummyAnalyze
import fr.hozakan.flysightcompanion.model.records.dummyRecordFile
import fr.hozakan.flysightcompanion.model.ui.PlotBottomItem
import fr.hozakan.flysightcompanion.model.ui.PlotDisplayPreference
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem

@Immutable
data class RecordDetailState(
    val recordFile: RecordFile = dummyRecordFile,
    val content: String = "",
    val analyze: RecordAnalyze = dummyAnalyze,
    val plotLeftItems: List<PlotLeftItem> = listOf(PlotLeftItem.Elevation),
    val plotBottomItem: PlotBottomItem = PlotBottomItem.Time,
    val plotDisplayPreferences: List<PlotDisplayPreference> = PlotDisplayPreference.defaultValues(),
    val unitSystem: UnitSystem = UnitSystem.Metric
)