package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.framework.math.computeGroundSpeed
import fr.hozakan.flysightcompanion.framework.math.computeTotalSpeed
import fr.hozakan.flysightcompanion.model.GnssData
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GnssFeedParserV1(
    private val log: (String) -> Unit
) : GnssFeedParser {
    override fun parse(value: ByteArray): GnssData? {
        if (value.size != 29) {
            log("Invalid GNSS PV data size")
            return null
        }
        val buffer = ByteBuffer.wrap(value.sliceArray(1 until value.size))
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val iTow = buffer.int.toUInt() //getInt(bytes, 0).toUInt()
        val lon = buffer.int // Raw int32 longitude value
        val lat = buffer.int // Raw int32 latitude value
        val hMsl = buffer.int //getInt(bytes, 12)
        val velN = buffer.int //getInt(bytes, 16)
        val velE = buffer.int //getInt(bytes, 20)
        val velD = buffer.int //getInt(bytes, 24)

        // Convert int32 to decimal degrees with 1e-7 scaling factor
        val latitudeDouble = lat * 1e-7
        val longitudeDouble = lon * 1e-7


        // Calculate ground speed from velN and velE (Pythagorean theorem)
        val groundSpeed = computeGroundSpeed(
            velN / 1_000.0,
            velE / 1_000.0
        )

        // Calculate total speed (3D) from velN, velE and velD
        val totalSpeed = computeTotalSpeed(
            velN / 1_000.0,
            velE / 1_000.0,
            velD / 1_000.0
        )

        val gnssData = GnssData(
            iTow = iTow,
            lon = longitudeDouble, // Keep the raw integer for backward compatibility
            lat = latitudeDouble, // Keep the raw integer for backward compatibility
            hMsl = hMsl / 1_000, // Convert from mm to meters
            velN = velN / 1_000,
            velE = velE / 1_000,
            velD = velD / 1_000,
            gpsFix = 3, // 3 is a threshold under which the data won't be used
            vAcc = 0,
            hAcc = 0,
            sAcc = 0,
            speed = totalSpeed,
            gSpeed = groundSpeed
        )
        log("GNSS data: $gnssData (lat=${latitudeDouble}, lon=${longitudeDouble})")
        return gnssData
    }
}