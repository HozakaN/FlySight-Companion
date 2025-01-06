package fr.hozakan.flysightcompanion.model.records

import fr.hozakan.flysightcompanion.model.extensions.formatDate
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import java.time.LocalDateTime

data class Record(
    /**
     * file path on the FlySight
     */
    val filePath: String,
    val dateTime: LocalDateTime
) {
    val fileName: String
        get() = "${dateTime.formatDate()}_${dateTime.formatTime()}_track.csv"
}