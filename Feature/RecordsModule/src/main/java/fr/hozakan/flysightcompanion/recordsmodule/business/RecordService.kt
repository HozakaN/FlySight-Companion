package fr.hozakan.flysightcompanion.recordsmodule.business

import fr.hozakan.flysightcompanion.model.records.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface RecordService {
    val records: StateFlow<List<Record>>
    suspend fun loadRecords()
    suspend fun createRecord(record: Record, trackFileContent: String)
    suspend fun deleteRecord(record: Record)
    suspend fun loadRecordContent(record: Record): String?
    fun formatRecordDateTimeFromPathParts(datePart: String, timePart: String): LocalDateTime
}