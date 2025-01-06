package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.records.Record
import fr.hozakan.flysightcompanion.model.records.dummyRecord

@Immutable
data class RecordDetailState(
    val record: Record = dummyRecord,
    val content: String = ""
)