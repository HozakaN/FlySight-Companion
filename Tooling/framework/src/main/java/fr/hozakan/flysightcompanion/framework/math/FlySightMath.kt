package fr.hozakan.flysightcompanion.framework.math

import fr.hozakan.flysightcompanion.model.session.configuration.Coordinate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * Calculate the horizontal distance between two points using the Haversine formula
 * Returns distance in nautical miles
 */
fun computeHorizontalDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadiusNm = 3440.065 // Earth radius in nautical miles

    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)

    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(lonDistance / 2) * sin(lonDistance / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    // Return horizontal distance in nautical miles
    return earthRadiusNm * c
}

fun computeHeading(from: Coordinate, to: Coordinate): Double {
    val dLng = Math.toRadians(to.longitude - from.longitude)
    val fromLat = Math.toRadians(from.latitude)
    val toLat = Math.toRadians(to.latitude)

    val y = sin(dLng) * cos(toLat)
    val x = cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng)
    return (atan2(y, x) + 2 * Math.PI) % (2 * Math.PI) // Normalize to [0, 2π)
}

/**
 * Calculate the signed perpendicular distance from a point to a line.
 * Returns negative for points to the left of the line, positive for points to the right.
 */
fun computeSignedDistanceToLine(
    point: Coordinate,
    lineStart: Coordinate,
    lineEnd: Coordinate
): Float {
    // Convert to local coordinates for easier calculation
    val earthRadius = 6378137.0 // meters

    // Convert degrees to radians
    val p1Lat = Math.toRadians(lineStart.latitude)
    val p1Lng = Math.toRadians(lineStart.longitude)
    val p2Lat = Math.toRadians(lineEnd.latitude)
    val p2Lng = Math.toRadians(lineEnd.longitude)
    val pLat = Math.toRadians(point.latitude)
    val pLng = Math.toRadians(point.longitude)

    // Convert to approximate flat earth coordinates (x,y in meters)
    val x1 = earthRadius * p1Lng * cos(p1Lat)
    val y1 = earthRadius * p1Lat
    val x2 = earthRadius * p2Lng * cos(p2Lat)
    val y2 = earthRadius * p2Lat
    val x = earthRadius * pLng * cos(pLat)
    val y = earthRadius * pLat

    // Calculate vector from line start to line end
    val dx = x2 - x1
    val dy = y2 - y1

    // Calculate signed distance using cross product
    // (p-p1) × (p2-p1) / |p2-p1|
    val crossProduct = (x - x1) * dy - (y - y1) * dx
    val lineLength = sqrt(dx * dx + dy * dy)

    // Distance is positive if point is to the right of the line, negative if to the left
    return (crossProduct / lineLength).toFloat()
}

/**
 * Calculate vertical speed between two points given their coordinates
 * Returns vertical speed in meters per second (m/s)
 * Positive values indicate ascent, negative values indicate descent
 * 
 * @param lat1 Latitude of the first point (degrees)
 * @param lon1 Longitude of the first point (degrees)
 * @param alt1 Altitude of the first point (meters)
 * @param lat2 Latitude of the second point (degrees)
 * @param lon2 Longitude of the second point (degrees)
 * @param alt2 Altitude of the second point (meters)
 * @param timeInterval Time between measurements in seconds
 * @return Vertical speed in meters per second
 */
fun computeVerticalSpeed(
    lat1: Double, lon1: Double, alt1: Double,
    lat2: Double, lon2: Double, alt2: Double,
    timeInterval: Double
): Float {
    // Calculate altitude difference (in meters)
    val altitudeDifference = alt2 - alt1
    
    // Calculate vertical speed (altitude change divided by time)
    val verticalSpeed = altitudeDifference / timeInterval
    
    return verticalSpeed.toFloat()
}

/**
 * Simplified version of computeVerticalSpeed that takes only elevations and time
 * Returns vertical speed in meters per second (m/s)
 * 
 * @param alt1 Altitude of the first point (meters)
 * @param alt2 Altitude of the second point (meters)
 * @param timeInterval Time between measurements in seconds
 * @return Vertical speed in meters per second
 */
fun computeVerticalSpeed(alt1: Double, alt2: Double, timeInterval: Double): Float {
    val altitudeDifference = alt2 - alt1
    return (altitudeDifference / timeInterval).toFloat()
}
