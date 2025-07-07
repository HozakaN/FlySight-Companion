package fr.hozakan.flysightcompanion.model.config

data class Navigation(
    val deviceId: String,       // Device ID (24 characters hex)
    val lat: Int,              // Latitude (degrees * 10,000,000)
    val lon: Int,              // Longitude (degrees * 10,000,000)
    val bearing: Int,          // Bearing (degrees)
    val endNav: Int,           // End navigation altitude (meters)
    val maxDist: Int,          // Maximum distance (meters)
    val minAngle: Int          // Minimum angle for direction (degrees)
)