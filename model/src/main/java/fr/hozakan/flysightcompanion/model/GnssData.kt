package fr.hozakan.flysightcompanion.model

import java.util.Locale

class GnssData(
    val iTow: UInt,
    val lon: Double,
    val lat: Double,
    val hMsl: Int,
    val velN: Int,
    val velE: Int,
    val velD: Int,
    val gpsFix: Int,
    val vAcc: Int,
    val gSpeed: Int,
    val speed: Int
) {
    override fun toString(): String {
        return String.format(
            Locale.ROOT,
            "%d, %.07f, %.07f, %.03f, %.03f, %.03f, %.03f",
            iTow.toInt(),
            lon.toFloat(),
            lat.toFloat(),
            hMsl,
            velN,
            velE,
            velD,
//            lon.toFloat() / 1e7,
//            lat.toFloat() / 1e7,
//            hMsl / 1e3,
//            velN / 1e3,
//            velE / 1e3,
//            velD / 1e3
        )
    }
}

val FakeGnssData = GnssData(
    iTow = 0u,
    lon = 0.0,
    lat = 0.0,
    hMsl = 0,
    velN = 0,
    velE = 0,
    velD = 0,
    gpsFix = 0,
    vAcc = 0,
    speed = 0,
    gSpeed = 0
)