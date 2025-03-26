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
            "%.03f, %.07f, %.07f, %.03f, %.03f, %.03f, %.03f",
            iTow.toDouble() / 1e3,
            lon.toDouble() / 1e7,
            lat.toDouble() / 1e7,
            hMsl.toDouble() / 1e3,
            velN.toDouble() / 1e3,
            velE.toDouble() / 1e3,
            velD.toDouble() / 1e3
        )
    }
}