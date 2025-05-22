package fr.hozakan.flysightcompanion.locationmodule

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightcompanion.framework.service.permission.PermissionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GPlayLocationService(
    private val context: Context,
    private val activityLifecycleService: ActivityLifecycleService,
//    private val gpsInfoService: GpsInfoService,
    private val androidPermissionsService: AndroidPermissionsService
) : LocationService {

    private val locationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            context
        )
    }

//    private var currentSatUsed = 0
//    private var currentSatInView = 0

    private val _locationAvailabilityState = MutableStateFlow(LocationAvailabilityState.ForegroundLocationNotAllowed)
    override val locationAvailabilityState: StateFlow<LocationAvailabilityState>
        get() = _locationAvailabilityState


    private val locationRequest by lazy {
        LocationRequest.Builder(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            200L
        )
            .build()
    }

    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    notifyNewLocation(location)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                notifyLocationAvailabilityChanged(locationAvailability.isLocationAvailable)
            }
        }
    }

//    private val gpsCallback: GpsInfoSystem.Callback by lazy {
//        object : GpsInfoSystem.Callback {
//            override fun onSatelliteStatusChanged(satCount: Int, satUsed: Int) {
//                currentSatUsed = satUsed
//                currentSatInView = satCount
//            }
//        }
//    }

    private val locationPermissionListener: (PermissionEvent) -> Unit = {
        if (it is PermissionEvent.LocationPermissionChanged) {
            scope.launch {
                _locationAvailabilityState.value = getLocationAvailability()
            }
        }
    }

    private val locationProviderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            scope.launch {
                _locationAvailabilityState.value = getLocationAvailability()
            }
        }

    }

    private val scope = CoroutineScope(Job())

    private val _events = MutableSharedFlow<LocationServiceEvent>()
    override val events: Flow<LocationServiceEvent>
        get() = _events

    private var isStarted = false

    init {
        scope.launch {
            _locationAvailabilityState.value = getLocationAvailability()
            _events.subscriptionCount
                .collect {
                    if (it > 0) {
                        startEmitting()
                    } else {
                        stopEmitting()
                    }
                }
        }
        androidPermissionsService += locationPermissionListener
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(locationProviderReceiver, filter)
    }

    private suspend fun getLocationAvailability(): LocationAvailabilityState {
        val foregroundLocationAllowed =
            androidPermissionsService.hasForegroundLocationPermission()
        if (!foregroundLocationAllowed) {
            return LocationAvailabilityState.ForegroundLocationNotAllowed
        }

//        if (Build.VERSION.SDK_INT >= 29) {
//            val backgroundLocationAllowed =
//                androidPermissionsService.hasBackgroundLocationPermission()
//            if (!backgroundLocationAllowed) {
//                return LocationAvailabilityState.BackgroundLocationNotAllowed
//            }
//        }

        val settingOn = isLocationSettingsEnabled()
        if (!settingOn) {
            return LocationAvailabilityState.SettingNotEnabled
        }
        return LocationAvailabilityState.LocationAvailable
    }

    override suspend fun isLocationSettingsEnabled(): Boolean = suspendCancellableCoroutine { continuation ->
        val task = LocationServices.getSettingsClient(context)
            .checkLocationSettings(with(LocationSettingsRequest.Builder()) {
                addLocationRequest(locationRequest)
                setAlwaysShow(true)
                build()
            })
        task.addOnCompleteListener { completedTask ->
            try {
                val response = completedTask.getResult(ApiException::class.java)
                val locationUsable = response.locationSettingsStates?.isLocationUsable
                continuation.resume(locationUsable ?: false)
            } catch (exception: ApiException) {
                continuation.resume(false)
            }
        }
        continuation.invokeOnCancellation {}
    }

    override suspend fun ensureLocationSettingsEnabled(): Boolean =
        suspendCancellableCoroutine { continuation ->
            val task = LocationServices.getSettingsClient(context)
                .checkLocationSettings(with(LocationSettingsRequest.Builder()) {
                    addLocationRequest(locationRequest)
                    setAlwaysShow(true)
                    build()
                })
            task.addOnCompleteListener { completedTask ->
                try {
                    val response = completedTask.getResult(ApiException::class.java)
                    continuation.resume(response.locationSettingsStates?.isLocationUsable ?: false)
                } catch (exception: ApiException) {
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            scope.launch {
                                val locationEnabled =
                                    (activityLifecycleService.currentActivity as? LocationCheckerActivity)?.enableLocation(
                                        exception as ResolvableApiException
                                    )
                                if (locationEnabled == true) {
                                    _locationAvailabilityState.value = LocationAvailabilityState.LocationAvailable
                                }
                                continuation.resume(locationEnabled == true)
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            continuation.resume(false)
                        }
                        else -> continuation.resume(false)
                    }
                }
            }
            continuation.invokeOnCancellation {}
        }

    private fun notifyNewLocation(location: Location) {
        scope.launch {
            _events.emit(
                LocationServiceEvent.NewLocationEvent(
                    location/*,
                    currentSatUsed,
                    currentSatInView*/
                )
            )
        }
    }

    private fun notifyLocationAvailabilityChanged(available: Boolean) {
//        delegate(LocationServiceEvent.LocationAvailabilityEvent(available))
    }

    private fun startEmitting() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
//        gpsInfoService.registerListener(gpsCallback)
        isStarted = true
    }

    private fun stopEmitting() {
        locationProviderClient.removeLocationUpdates(locationCallback)
//        gpsInfoService.unregisterListener(gpsCallback)
        isStarted = false
    }

}
