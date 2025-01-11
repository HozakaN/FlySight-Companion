package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.records.RecordAnalyze
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.model.records.dummyAnalyze
import fr.hozakan.flysightcompanion.model.records.dummyRecordFile
import fr.hozakan.flysightcompanion.model.ui.PlotBottomItem
import fr.hozakan.flysightcompanion.model.ui.PlotDisplayPreferences
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem
import fr.hozakan.flysightcompanion.model.ui.defaultDisplayPreferences

@Immutable
data class RecordDetailState(
    val recordFile: RecordFile = dummyRecordFile,
    val content: String = "",
    val analyze: RecordAnalyze = dummyAnalyze,
    val plotLeftItems: List<PlotLeftItem> = listOf(PlotLeftItem.Elevation),
    val plotBottomItem: PlotBottomItem = PlotBottomItem.Time,
    val plotDisplayPreferences: PlotDisplayPreferences = defaultDisplayPreferences
)