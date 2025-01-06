package fr.hozakan.flysightcompanion.model.records

import fr.hozakan.flysightcompanion.model.extensions.formatDate
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import java.time.LocalDateTime

data class Record(
    val dateTime: LocalDateTime
) {
    val flySightFilePath: String
        get() = if (this == dummyRecord) "Dummy" else "/${dateTime.formatDate()}/${dateTime.formatTime()}/TRACK.CSV"

    val phoneFilePath: String
        get() = if (this == dummyRecord) "Dummy" else "${dateTime.formatDate()}_${dateTime.formatTime()}_TRACK.CSV"

}

val dummyRecord = Record(
    dateTime = LocalDateTime.now()
)