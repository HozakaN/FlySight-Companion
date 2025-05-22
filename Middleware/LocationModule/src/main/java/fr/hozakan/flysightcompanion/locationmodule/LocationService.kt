package fr.hozakan.flysightcompanion.locationmodule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocationService {
    val locationAvailabilityState: StateFlow<LocationAvailabilityState>
    val events: Flow<LocationServiceEvent>
    suspend fun ensureLocationSettingsEnabled(): Boolean
    suspend fun isLocationSettingsEnabled(): Boolean
}
