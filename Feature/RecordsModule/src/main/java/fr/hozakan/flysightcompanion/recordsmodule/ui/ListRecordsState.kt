package fr.hozakan.flysightcompanion.recordsmodule.ui

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.records.Record

@Immutable
data class ListRecordsState(
    val records: List<Record> = emptyList()
)