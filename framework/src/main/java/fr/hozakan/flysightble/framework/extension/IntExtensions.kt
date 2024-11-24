package fr.hozakan.flysightble.framework.extension

import fr.hozakan.flysightble.model.config.UnitSystem

fun Int.toBoolean(): Boolean {
    return this != 0
}

fun Int.speedInUnit(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        UnitSystem.Metric -> (this * 0.036).toInt()
        UnitSystem.Imperial -> (this * 0.036 * 0.621371).toInt()
    }
}

fun Int.fromSpeedUnitToCmPerSec(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        UnitSystem.Metric -> this.div(0.036).toInt()
        UnitSystem.Imperial -> ((this / 0.621371) / 0.036).toInt()
    }
}

fun Int.distanceInUnit(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        UnitSystem.Metric -> this
        UnitSystem.Imperial -> (this * 3.28084).toInt()
    }
}

fun Int.fromDistanceUnitToMeter(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        UnitSystem.Metric -> this
        UnitSystem.Imperial -> (this / 3.28084).toInt()
    }
}