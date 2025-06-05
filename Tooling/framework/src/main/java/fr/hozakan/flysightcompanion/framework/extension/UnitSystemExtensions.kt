package fr.hozakan.flysightcompanion.framework.extension

import fr.hozakan.flysightcompanion.model.config.UnitSystem

/**
 * Transform from speed in cm/s to current unit (either mph or km/h)
 */
fun Int.speedInUnit(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        UnitSystem.Metric -> (this * 0.036).toInt()
        UnitSystem.Imperial -> (this * 0.036 * 0.621371).toInt()
    }
}

fun Double.speedInUnit(unitSystem: UnitSystem): Double {
    return when (unitSystem) {
        UnitSystem.Metric -> (this * 0.036)
        UnitSystem.Imperial -> (this * 0.036 * 0.621371)
    }
}

/**
 * Transform from speed in current unit (either mph or km/h) to cm/s
 */
fun Int.fromSpeedUnitToCmPerSec(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        //we are in km/h
        UnitSystem.Metric -> this.div(0.036).toInt()
        //we are in mph
        UnitSystem.Imperial -> ((this / 0.621371) / 0.036).toInt()
    }
}

fun Double.fromSpeedUnitToCmPerSec(unitSystem: UnitSystem): Double {
    return when (unitSystem) {
        //we are in km/h
        UnitSystem.Metric -> this.div(0.036)
        //we are in mph
        UnitSystem.Imperial -> ((this / 0.621371) / 0.036)
    }
}

/**
 * Transform from distance in meters to current unit (either feet or meters)
 */
fun Int.distanceInUnit(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        UnitSystem.Metric -> this
        UnitSystem.Imperial -> (this * 3.28084).toInt()
    }
}
fun Double.distanceInUnit(unitSystem: UnitSystem): Double {
    return when (unitSystem) {
        UnitSystem.Metric -> this
        UnitSystem.Imperial -> (this * 3.28084)
    }
}

/**
 * Transform from distance in current unit (either feet or meters) to meters
 */
fun Int.fromDistanceUnitToMeter(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        //we are in meters
        UnitSystem.Metric -> this
        //we are in feet
        UnitSystem.Imperial -> (this / 3.28084).toInt()
    }
}
fun Double.fromDistanceUnitToMeter(unitSystem: UnitSystem): Double {
    return when (unitSystem) {
        //we are in meters
        UnitSystem.Metric -> this
        //we are in feet
        UnitSystem.Imperial -> (this / 3.28084)
    }
}