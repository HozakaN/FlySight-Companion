package fr.hozakan.flysightcompanion.recordsmodule.business

import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface RecordService {
    val records: StateFlow<List<RecordFile>>
    suspend fun loadRecords()
    suspend fun createRecord(recordFile: RecordFile, trackFileContent: String)
    suspend fun deleteRecord(recordFile: RecordFile)
    suspend fun loadRecordContent(recordFile: RecordFile): String?
    fun formatRecordDateTimeFromPathParts(datePart: String, timePart: String): LocalDateTime
}