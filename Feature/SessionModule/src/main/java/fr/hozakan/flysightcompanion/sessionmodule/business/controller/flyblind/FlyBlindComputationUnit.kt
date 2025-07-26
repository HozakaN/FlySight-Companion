package fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind

import fr.hozakan.flysightcompanion.framework.math.computeHeading
import fr.hozakan.flysightcompanion.framework.math.computeHorizontalDistance
import fr.hozakan.flysightcompanion.framework.math.radToDeg
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.model.session.profile.Coordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class FlyBlindComputationUnit(
    private val configuration: FlyBlindConfiguration
) {

    private val _distance = MutableStateFlow(0.0)
    val distance: StateFlow<Double> = _distance.asStateFlow()

    private val _heading = MutableStateFlow(0.0)
    val heading: StateFlow<Double> = _heading.asStateFlow()

    private var previousData: GnssData? = null

    public fun reset() {
        _distance.value = 0.0
        _heading.value = 0.0
    }

//    fun handleDataBatch(gnssData: List<GnssData>) {
//        reset()
//        gnssData.lastOrNull()?.let { data ->
//            handleNewData(data)
//        }
//    }

    fun handleNewData(data: GnssData) {
        if (data.gpsFix >= 3) {
            val previous = previousData
            if (previous == null) {
                previousData = data
                return
            }
            val refPoint = configuration.referencePoint
            _distance.value = computeHorizontalDistance(
                lat1 = data.lat,
                lon1 = data.lon,
                lat2 = refPoint.coords.latitude,
                lon2 = refPoint.coords.longitude
            )
            val currentCoord = Coordinate(
                latitude = data.lat,
                longitude = data.lon
            )
            val refPointHeading = computeHeading(
                from = currentCoord,
                to = refPoint.coords
            ).radToDeg()
            val flyerHeading = computeHeading(
                from = Coordinate(
                    latitude = previous.lat,
                    longitude = previous.lon
                ),
                to = currentCoord
            ).radToDeg()
            _heading.value = (refPointHeading - flyerHeading) * if (configuration.isBellyFlying) 1 else -1
            previousData = data
        }
    }
}