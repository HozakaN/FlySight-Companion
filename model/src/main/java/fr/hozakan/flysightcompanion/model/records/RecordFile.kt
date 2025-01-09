package fr.hozakan.flysightcompanion.model.records

import fr.hozakan.flysightcompanion.model.extensions.formatDate
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import java.time.LocalDateTime

data class RecordFile(
    val dateTime: LocalDateTime
) {
    val flySightFilePath: String
        get() = if (this == dummyRecordFile) "Dummy" else "/${dateTime.formatDate()}/${dateTime.formatTime()}/TRACK.CSV"

    val phoneFilePath: String
        get() = if (this == dummyRecordFile) "Dummy" else "${dateTime.formatDate()}_${dateTime.formatTime()}_TRACK.CSV"

}

val dummyRecordFile = RecordFile(
    dateTime = LocalDateTime.now()
)