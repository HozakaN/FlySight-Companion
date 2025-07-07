package fr.hozakan.flysightcompanion.recordsmodule.business

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.model.records.RecordAnalyze
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.recordsmodule.business.analyze.DefaultRecordAnalyzer
import fr.hozakan.flysightcompanion.recordsmodule.business.analyze.DefaultRecordParser
import fr.hozakan.flysightcompanion.recordsmodule.business.analyze.RecordParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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

    override fun formatRecordDateTimeFromPathParts(
        datePart: String,
        timePart: String
    ): LocalDateTime {
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

    override suspend fun loadRecordRawContent(recordFile: RecordFile): String? {
        val trackFile =
            File("${getOrCreateRecordsFolder().absolutePath}${File.separator}${recordFile.phoneFilePath}")
        return if (trackFile.exists()) trackFile.readText() else null
//        return javaClass.classLoader
//            ?.getResource("RECORD_beaufort_jump_9_2_temps.CSV")?.readText() ?: ""
    }

    override suspend fun analyzeRecord(recordFile: RecordFile): RecordAnalyze {
        val rawContent =
            loadRecordRawContent(recordFile) ?: return RecordAnalyze.error("Record is empty")
//        val fileContent = javaClass.classLoader
//            ?.getResource("RECORD_beaufort_jump_9_2_temps.CSV")?.readText() ?: ""
        val parser = DefaultRecordParser()
        val analyzer = DefaultRecordAnalyzer()
        val dataPoints = parser.parse(rawContent.lines())
        val analyze = analyzer.analyze(
            dataPoints = dataPoints,
        )
        return analyze
    }

    override suspend fun exportRecord(recordFile: RecordFile) {
        val fileContent = loadRecordRawContent(recordFile)
        if (fileContent != null) {
            val outStream = context.openFileOutput("track.csv", Context.MODE_PRIVATE)
            outStream.write(fileContent.toByteArray())
            outStream.close()

            val file = File("${context.filesDir}", "track.csv")
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val trackIntent = Intent(Intent.ACTION_SEND)
            trackIntent.type = "text/csv"
            trackIntent.putExtra(Intent.EXTRA_STREAM, uri)
            trackIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val shareIntent = Intent.createChooser(trackIntent, "Share track file")
            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(shareIntent)

        }
    }

    override suspend fun saveToDownloads(recordFile: RecordFile): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileContent = loadRecordRawContent(recordFile) ?: return@withContext false
                
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                
                val fileName = recordFile.phoneFilePath
                val file = File(downloadsDir, fileName)
                file.writeText(fileContent)

                // Notify MediaStore about the new file so it appears in Files app
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("text/csv"),
                    null
                )

                true
            } catch (e: Exception) {
                Timber.e(e, "Hoz3 ${e.message}")
                false
            }
        }
    }

//    override fun getFile(recordFile: RecordFile): File {
//        return File("${getOrCreateRecordsFolder().absolutePath}${File.separator}${recordFile.phoneFilePath}")
//    }

    private fun getOrCreateRecordsFolder(): File {
        val folder =
            File("${context.filesDir.absolutePath}${File.separator}$RECORDS_FOLDER")
        val success = folder.exists() || (folder.mkdir() && addDefaultResults())
        return if (success) folder else throw IllegalAccessException("Cannot access app folder")
    }

    private fun addDefaultResults(): Boolean {
        scope.launch(Dispatchers.IO) {
            val fileContent = javaClass.classLoader
                ?.getResource("RECORD_beaufort_jump_9_2_temps.CSV")?.readText() ?: ""
            createRecord(
                recordFile = RecordFile(
                    dateTime = LocalDateTime.parse("24-11-23_20-38-56", dateTimeFormatter)
                ),
                trackFileContent = fileContent
            )
        }
        return true
    }

    companion object {
        private const val RECORDS_FOLDER = "records"
    }
}