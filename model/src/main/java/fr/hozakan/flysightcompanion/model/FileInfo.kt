package fr.hozakan.flysightcompanion.model

import java.util.Date

data class FileInfo(
    val packetId: Int,
    val fileSize: Long,
    val fileDate: Date,
    val fileTime: Date,
    val fileAttributes: Set<String>,
    val fileName: String,
    val isDirectory: Boolean
)