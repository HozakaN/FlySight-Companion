package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.framework.math.computeGroundSpeed
import fr.hozakan.flysightcompanion.framework.math.computeTotalSpeed
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.GnssMask
import fr.hozakan.flysightcompanion.model.GnssData
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GnssFeedParserV2(
    private val log: (String) -> Unit
) : GnssFeedParser {
    override fun parse(value: ByteArray): GnssData? {
        if (value.isEmpty()) {
            log("Empty GNSS data received")
            return null
        }

        // First byte is the mask
        val mask = value[0].toUByte()
        val offset = 1

        log("Parsing GNSS data with mask: 0x${mask.toString(16).uppercase()} (${GnssMask.describe(mask)})")

        // Initialize default values
        var iTow: UInt = 0u
        var lon: Int = 0
        var lat: Int = 0
        var hMsl: Int = 0
        var velN: Int = 0
        var velE: Int = 0
        var velD: Int = 0
        var hAcc: Int = 0
        var vAcc: Int = 0
        var sAcc: Int = 0
        var numSV: Int = 0

        val buffer = ByteBuffer.wrap(value, offset, value.size - offset)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        try {
            // Parse fields based on mask (MSB first order)
            
            if (GnssMask.isEnabled(mask, GnssMask.TOW)) {
                if (buffer.remaining() < 4) throw IllegalArgumentException("Not enough data for TOW")
                iTow = buffer.int.toUInt()
                log("TOW: $iTow")
            }

            if (GnssMask.isEnabled(mask, GnssMask.WEEK)) {
                // Week number not yet implemented in firmware, but reserve space if needed
                log("WEEK field enabled but not implemented in firmware")
            }

            if (GnssMask.isEnabled(mask, GnssMask.POSITION)) {
                if (buffer.remaining() < 12) throw IllegalArgumentException("Not enough data for POSITION")
                lon = buffer.int
                lat = buffer.int
                hMsl = buffer.int
                log("Position: lon=$lon, lat=$lat, hMsl=$hMsl")
            }

            if (GnssMask.isEnabled(mask, GnssMask.VELOCITY)) {
                if (buffer.remaining() < 12) throw IllegalArgumentException("Not enough data for VELOCITY")
                velN = buffer.int
                velE = buffer.int
                velD = buffer.int
                log("Velocity: velN=$velN, velE=$velE, velD=$velD")
            }

            if (GnssMask.isEnabled(mask, GnssMask.ACCURACY)) {
                if (buffer.remaining() < 12) throw IllegalArgumentException("Not enough data for ACCURACY")
                hAcc = buffer.int
                vAcc = buffer.int
                sAcc = buffer.int
                log("Accuracy: hAcc=$hAcc, vAcc=$vAcc, sAcc=$sAcc")
            }

            if (GnssMask.isEnabled(mask, GnssMask.NUM_SV)) {
                if (buffer.remaining() < 1) throw IllegalArgumentException("Not enough data for NUM_SV")
                numSV = buffer.get().toInt() and 0xFF
                log("Number of satellites: $numSV")
            }

        } catch (e: Exception) {
            log("Error parsing GNSS data: ${e.message}")
            return null
        }

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
            lon = longitudeDouble,
            lat = latitudeDouble,
            hMsl = hMsl / 1_000, // Convert from mm to meters
            velN = velN / 1_000,
            velE = velE / 1_000,
            velD = velD / 1_000,
            gpsFix = numSV, // GPS fix not included in this data format
            vAcc = vAcc / 1_000,
            hAcc = hAcc / 1_000,
            sAcc = sAcc / 1_000,
            speed = totalSpeed,
            gSpeed = groundSpeed
        )
        
        log("GNSS data: $gnssData (lat=${latitudeDouble}, lon=${longitudeDouble})")
        return gnssData
    }
}