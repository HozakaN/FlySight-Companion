package fr.hozakan.flysightcompanion.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Log(
    val message: String
) {
    val time: LocalTime = LocalTime.now()

    val computedMessage: String
        get() = "[${time.format(DateTimeFormatter.ISO_LOCAL_TIME)}] $message"

}