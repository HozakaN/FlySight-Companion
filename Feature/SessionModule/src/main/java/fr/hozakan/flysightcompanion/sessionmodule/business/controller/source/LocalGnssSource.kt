package fr.hozakan.flysightcompanion.sessionmodule.business.controller.source

import android.location.Location
import fr.hozakan.flysightcompanion.locationmodule.LocationService
import fr.hozakan.flysightcompanion.locationmodule.LocationServiceEvent
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.math.cos
import kotlin.math.sin

class LocalGnssSource(
    locationService: LocationService
) : GnssSource {

    private var prevLocation: Location? = null

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("LocalGnssSource") + Dispatchers.IO)

    override val gnssFlow: SharedFlow<GnssData> = locationService
        .events
        .filterIsInstance<LocationServiceEvent.NewLocationEvent>()
        .map { event ->
            // Convert location data to GnssData
            val velD = prevLocation?.let { previous ->
                -1 * computeVerticalVelocityMetersPerSecond(
                    previous = previous,
                    current = event.location
                ).toInt()
            } ?: 0

            GnssData(
                iTow = (System.currentTimeMillis() % (7 * 24 * 3600 * 1000)).toUInt(), // GPS Time of Week in milliseconds
                lon = event.location.longitude,
                lat = event.location.latitude,
                hMsl = event.location.altitude.toInt(), // MSL altitude in mm
                velN = (event.location.speed * cos(Math.toRadians(event.location.bearing.toDouble()))).toInt(), // North velocity component in mm/s
                velE = (event.location.speed * sin(Math.toRadians(event.location.bearing.toDouble()))).toInt(), // East velocity component in mm/s
                velD = velD,
                gpsFix = if (event.location.accuracy <= 8) 3 else if (event.location.accuracy <= 15) 2 else 1, // GPS fix quality based on accuracy
                vAcc = event.location.verticalAccuracyMeters.toInt(), // Vertical accuracy in mm
                hAcc = event.location.accuracy.toInt(),
                sAcc = event.location.speedAccuracyMetersPerSecond.toInt(),
                gSpeed = event.location.speed.toInt(), // Ground speed in mm/s
                speed = event.location.speed.toInt() // 3D speed in mm/s (same as ground speed if vertical component isn't available)
            )
        }.shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed()
        )

    override val timeMutableSource: TimeMutableSource? = null

    private fun computeVerticalVelocityMetersPerSecond(
        previous: Location,
        current: Location
    ): Double {
        val altitudeDelta = current.altitude - previous.altitude // in meters

        val timeDeltaMillis = current.time - previous.time
        if (timeDeltaMillis <= 0) return 0.0

        val timeDeltaSeconds = timeDeltaMillis / 1000.0

        return altitudeDelta / timeDeltaSeconds // m/s
    }

}
