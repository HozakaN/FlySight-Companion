package fr.hozakan.flysightcompanion.model

import java.util.Locale

class GnssData(
    val iTow: UInt,
    val lon: Int,
    val lat: Int,
    val hMsl: Int,
    val velN: Int,
    val velE: Int,
    val velD: Int
) {
    override fun toString(): String {
        return String.format(
            Locale.ROOT,
            "%d, %.07f, %.07f, %.03f, %.03f, %.03f, %.03f",
            iTow.toInt(),
            lon.toFloat() / 1e7,
            lat.toFloat() / 1e7,
            hMsl / 1e3,
            velN / 1e3,
            velE / 1e3,
            velD / 1e3
        )
    }
}