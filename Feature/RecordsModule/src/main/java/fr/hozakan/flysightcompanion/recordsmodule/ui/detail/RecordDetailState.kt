package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.model.records.dummyRecordFile

@Immutable
data class RecordDetailState(
    val recordFile: RecordFile = dummyRecordFile,
    val content: String = ""
)