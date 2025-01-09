package fr.hozakan.flysightcompanion.recordsmodule.business

import android.content.Context
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val record_name_regex =
    "^\\d{2}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_TRACK.CSV".toRegex()

private val record_name_rege2x =
    "^(\\d{2})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$".toRegex()

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yy-MM-dd_HH-mm-ss")


class FileBasedRecordService(
    private val context: Context
) : RecordService {

    private val parser: RecordParser = DefaultRecordParser()

    private val _records = MutableStateFlow(emptyList<RecordFile>())
    override val records: StateFlow<List<RecordFile>> = _records.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            loadRecords()
        }
    }

    override suspend fun loadRecords() {
        withContext(Dispatchers.IO) {
            val recordFiles = getOrCreateRecordsFolder()
                .listFiles { file ->
                    file.isFile && file.name.matches(record_name_regex)
                }
                ?.mapNotNull { file ->
                    RecordFile(
                        dateTime = LocalDateTime.parse(
                            file.name.substring(0, 17),
                            dateTimeFormatter
                        )
                    )
                }
            _records.update {
                recordFiles ?: emptyList()
            }
        }
    }

    override fun formatRecordDateTimeFromPathParts(datePart: String, timePart: String): LocalDateTime {
        return LocalDateTime.parse("${datePart}_$timePart", dateTimeFormatter)
    }

    override suspend fun createRecord(recordFile: RecordFile, trackFileContent: String) {
        withContext(Dispatchers.IO) {
            val trackFile =
                File("${getOrCreateRecordsFolder().absolutePath}${File.separator}${recordFile.phoneFilePath}")
            trackFile.writeText(trackFileContent)
            _records.update {
                it + recordFile
            }
        }
    }

    override suspend fun deleteRecord(recordFile: RecordFile) {
        _records.update {
            it - recordFile
        }
        withContext(Dispatchers.IO) {
            File("${getOrCreateRecordsFolder().absolutePath}${File.separator}${recordFile.phoneFilePath}").delete()
        }
    }

    override suspend fun loadRecordContent(recordFile: RecordFile): String? {
        val trackFile =
            File("${getOrCreateRecordsFolder().absolutePath}${File.separator}${recordFile.phoneFilePath}")
        return if (trackFile.exists()) trackFile.readText() else null
    }

    private fun getOrCreateRecordsFolder(): File {
        val folder =
            File("${context.filesDir.absolutePath}${File.separator}$RECORDS_FOLDER")
        val success = folder.exists() || folder.mkdir()
        return if (success) folder else throw IllegalAccessException("Cannot access app folder")
    }

    companion object {
        private const val RECORDS_FOLDER = "records"
    }
}