package fr.hozakan.flysightcompanion.model.records

import java.time.LocalDateTime

typealias DataPoints = List<DataPoint>

data class DataPoint(
    val dateTime: LocalDateTime,
    val hasGeodetic: Boolean,
    val latitude: Double,
    val longitude: Double,
    val hMSL: Double,
    val velN: Double,
    val velE: Double,
    val velD: Double,
    val hAcc: Double,
    val vAcc: Double,
    val sAcc: Double,
    val numSV: Int
)