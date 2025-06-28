package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

/**
 * GNSS BLE Mask definitions based on FlySight 2 firmware
 * Controls which GNSS data fields are included in the real-time sensor data stream
 */
object GnssMask {
    
    /** Time of Week */
    const val TOW: UByte = 0x80u
    
    /** Week Number (not yet implemented in firmware) */
    const val WEEK: UByte = 0x40u
    
    /** Position (Longitude, Latitude, Height) */
    const val POSITION: UByte = 0x20u
    
    /** Velocity (North, East, Down) */
    const val VELOCITY: UByte = 0x10u
    
    /** Accuracy estimates */
    const val ACCURACY: UByte = 0x08u
    
    /** Number of satellites */
    const val NUM_SV: UByte = 0x04u
    
    /** Default mask value (TOW + Position + Velocity)  0xB0u*/
    val DEFAULT: UByte = (TOW or POSITION or VELOCITY)
    
    /** All available fields mask */
    val ALL: UByte = (TOW or POSITION or VELOCITY or ACCURACY or NUM_SV)
    
    /**
     * Check if a specific field is enabled in the mask
     */
    fun isEnabled(mask: UByte, field: UByte): Boolean {
        return (mask and field) != 0u.toUByte()
    }
    
    /**
     * Enable a specific field in the mask
     */
    fun enable(mask: UByte, field: UByte): UByte {
        return (mask or field).toUByte()
    }
    
    /**
     * Disable a specific field in the mask
     */
    fun disable(mask: UByte, field: UByte): UByte {
        return (mask and field.inv()).toUByte()
    }
    
    /**
     * Get a human-readable description of the mask
     */
    fun describe(mask: UByte): String {
        val fields = mutableListOf<String>()
        if (isEnabled(mask, TOW)) fields.add("TOW")
        if (isEnabled(mask, WEEK)) fields.add("WEEK")
        if (isEnabled(mask, POSITION)) fields.add("POSITION")
        if (isEnabled(mask, VELOCITY)) fields.add("VELOCITY")
        if (isEnabled(mask, ACCURACY)) fields.add("ACCURACY")
        if (isEnabled(mask, NUM_SV)) fields.add("NUM_SV")
        return fields.joinToString(" | ")
    }
}