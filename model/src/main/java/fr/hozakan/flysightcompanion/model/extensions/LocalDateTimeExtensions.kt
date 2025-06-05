package fr.hozakan.flysightcompanion.model.extensions

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yy-MM-dd")
private val timeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss")

fun LocalDateTime.formatDate(): String = this.format(dateFormatter)
fun LocalDateTime.formatTime(): String = this.format(timeFormatter)
fun LocalDateTime.toEpochMillisecond(offset: ZoneOffset): Long {
    return this.toInstant(offset).toEpochMilli()
}