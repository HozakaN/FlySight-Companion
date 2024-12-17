package fr.hozakan.flysightble.model.result

import java.time.LocalDateTime

data class ResultFile(
    val filePath: String,
    val dateTime: LocalDateTime
)
