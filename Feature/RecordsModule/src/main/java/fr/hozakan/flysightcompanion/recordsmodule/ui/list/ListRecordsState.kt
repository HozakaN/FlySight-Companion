package fr.hozakan.flysightcompanion.recordsmodule.ui.list

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.records.RecordFile

@Immutable
data class ListRecordsState(
    val recordFiles: List<RecordFile> = emptyList()
)