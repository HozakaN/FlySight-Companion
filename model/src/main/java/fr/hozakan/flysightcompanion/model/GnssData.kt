package fr.hozakan.flysightcompanion.model

import java.util.Locale

class GnssData(
    val iTow: UInt,
    val lon: Double,
    val lat: Double,
    val hMsl: Int, // m
    val velN: Int, // m/s
    val velE: Int, // m/s
    val velD: Int, // m/s
    val gpsFix: Int,
    val vAcc: Int,
    val gSpeed: Int, // m/s
    val speed: Int // m/s
) {
    override fun toString(): String {
        return String.format(
            Locale.ROOT,
//            "%d, %.07f, %.07f, %.03f, %.03f, %.03f, %.03f",
            "%d, %.07f, %.07f, %d, %d, %d, %d",
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

    fun readableTime(): String {
        // iTow is GPS Time of Week in milliseconds
        // Convert iTow back to time components
        val iTowMs = iTow.toInt()
        
        // Extract day of week (0 = Sunday, 1 = Monday, etc.)
        val dayOfWeek = iTowMs / (24 * 3600 * 1000)
        val msInDay = iTowMs % (24 * 3600 * 1000)
        
        // Extract hours, minutes, seconds and milliseconds
        val hours = msInDay / (3600 * 1000)
        val minutes = (msInDay % (3600 * 1000)) / (60 * 1000)
        val seconds = (msInDay % (60 * 1000)) / 1000
        val milliseconds = msInDay % 1000
        
        // Day names
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayName = dayNames[dayOfWeek]
        
        // Format the time as hh:mm:ss.ms
        return String.format(
            Locale.ROOT,
            "%s %02d:%02d:%02d.%03d",
            dayName,
            hours,
            minutes,
            seconds,
            milliseconds
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
