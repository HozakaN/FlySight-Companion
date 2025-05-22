package fr.hozakan.flysightcompanion.locationmodule

import android.location.Location

sealed class LocationServiceEvent {
    data class NewLocationEvent(val location: Location/*, val satUsed: Int, val satCount: Int*/) : LocationServiceEvent()
//    data class LocationAvailabilityEvent(val truc: String) : LocationServiceEvent()
}
