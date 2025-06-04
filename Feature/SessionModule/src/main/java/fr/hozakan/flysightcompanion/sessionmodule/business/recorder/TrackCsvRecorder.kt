package fr.hozakan.flysightcompanion.sessionmodule.business.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.edit
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.extensions.formatDate
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.GnssSource
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

class TrackCsvRecorder(
    private val context: Context,
    private val recordService: RecordService,
    private val gnssSource: GnssSource,
    private val appVersionService: AppVersionService
) : Recorder {

    private val sharedPrefs = context.getSharedPreferences("temp", Context.MODE_PRIVATE)

    private var counter = sharedPrefs.getInt("counter", 0)

    private var firstStart = true

    private var currentFolder: File? = null
    private var tmpFile = MutableStateFlow<File?>(null)

    private val dateTime = LocalDateTime.now()

    private val scope =
        CoroutineScope(SupervisorJob() + CoroutineName("TrackCsvRecorder") + Dispatchers.IO)

    override fun start() {
        if (firstStart) {
            firstStart = false
            scope.launch {
                val folder =
                    File("${getOrCreateTempFolder().absolutePath}${File.separator}$counter")
                if (folder.exists() || folder.mkdir()) {
                    sharedPrefs.edit { putInt("counter", counter + 1) }
                    currentFolder = folder
                    val file =
                        File("${folder.absolutePath}${File.separator}${dateTime.formatDate()}_${dateTime.formatTime()}_TRACK.CSV")
                    tmpFile.value = file
                    file.createNewFile()
                    val text = "\$FLYS,1\n" +
                            "\$VAR,FIRMWARE_VER,${appVersionService.appVersion}\n" +
                            "\$VAR,DEVICE_ID,${getAndroidId()}\n" +
                            "\$VAR,SESSION_ID,${dateTimeFormatter.format(dateTime.atOffset(ZoneOffset.UTC))}\n" +
                            "\$COL,GNSS,time,lat,lon,hMSL,velN,velE,velD,hAcc,vAcc,sAcc,numSV\n" +
                            "\$UNIT,GNSS,,deg,deg,m,m/s,m/s,m/s,m,m,m/s,\n" +
                            "\$DATA\n"
                    file.appendText(text)
                } else {
                    throw IllegalAccessException("Cannot access temp folder")
                }
            }
        }
        combine(tmpFile, gnssSource.gnssFlow) { file, data ->
            if (file != null) {
                append(file, data)
            } else {
                Timber.i("Temporary file is null, cannot append data")
            }
        }
            .launchIn(scope)
    }

    private fun append(file: File, data: GnssData) {
        try {
            val csvLine = formatGnssDataAsCsv(data)
            val text = "$csvLine\n"
            file.appendText(text)
        } catch (e: Exception) {
            Timber.e(e, "Failed to append data to file: ${file.absolutePath}")
        }
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: UUID.randomUUID().toString()  // Fallback if null
    }

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

    private fun convertITowToLocalDateTime(iTow: UInt): LocalDateTime {
        // GPS epoch started on January 6, 1980
        val gpsEpochStart = LocalDateTime.of(1980, 1, 6, 0, 0, 0)

        // GPS time rolls over every week (604,800,000 milliseconds)
        val millisInWeek = 604_800_000

        // iTow is milliseconds into the current GPS week
        val msIntoWeek = iTow.toInt() % millisInWeek

        // Get current time to determine the current week
        val now = LocalDateTime.now()

        // Calculate approximate number of weeks since GPS epoch
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(gpsEpochStart, now)
        val approxWeeks = daysBetween / 7

        // Calculate a timestamp that's in the current week
        var gpsTime = gpsEpochStart.plusWeeks(approxWeeks).plusNanos(msIntoWeek * 1_000_000L)

        // Adjust if needed - ensure the time is not in the future or too far in the past
        if (gpsTime.isAfter(now.plusDays(1))) {
            gpsTime = gpsTime.minusWeeks(1)
        } else if (gpsTime.isBefore(now.minusDays(6))) {
            gpsTime = gpsTime.plusWeeks(1)
        }

        return gpsTime
    }

    private fun formatGnssDataAsCsv(data: GnssData): String {
        val dateTime = convertITowToLocalDateTime(data.iTow)
        //$GNSS,2024-08-23T14:23:47.200Z,45.0762957,3.7640123,809.610,-1.503,-0.799,3.429,176.395,118.890,8.991,4
        return "\$GNSS," +
                "${dateTimeFormatter.format(dateTime.atOffset(ZoneOffset.UTC))}," +
                "${data.lat}," +
                "${data.lon}," +
                "${data.hMsl}," +
                "${data.velN}," +
                "${data.velE}," +
                "${data.velD}," +
//                "${data.hAcc}," +
                "0.0," +
                "${data.vAcc}," +
//                "${data.sAcc}," +
                "0.0," +
                "${data.gpsFix}"
    }

    override fun pause() {}

    override fun stop() {
        scope.launch {
            tmpFile.value?.readText().also { readText ->
                if (readText != null) {
                    recordService
                        .createRecord(
                            recordFile = RecordFile(
                                dateTime = dateTime
                            ),
                            trackFileContent = readText
                        )
                }
                val folder =
                    File("${getOrCreateTempFolder().absolutePath}${File.separator}$counter")
                folder.deleteRecursively()
//                tmpFile.value?.delete()
            }
            scope.cancel()
        }
    }

    private suspend fun getOrCreateTempFolder(): File {
        val folder =
            File("${context.filesDir.absolutePath}${File.separator}$TEMPS_FOLDER")
        val success = folder.exists() || (folder.mkdir())
        return if (success) folder else throw IllegalAccessException("Cannot access temp folder")
    }

    companion object {
        private const val TEMPS_FOLDER = "temp"
    }
}