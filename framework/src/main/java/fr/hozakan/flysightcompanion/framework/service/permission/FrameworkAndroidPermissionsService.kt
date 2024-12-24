package fr.hozakan.flysightcompanion.framework.service.permission

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import fr.hozakan.flysightcompanion.framework.service.ListenableService
import fr.hozakan.flysightcompanion.framework.service.MonitorableService
import fr.hozakan.flysightcompanion.framework.service.applifecycle.SimpleActivityLifecycleCallbacks
import fr.hozakan.flysightcompanion.framework.service.async.ActivityOperationsService
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FrameworkAndroidPermissionsService(
    private val delegate: MonitorableService<PermissionEvent>,
    private val application: Application,
    private val activityOperationsService: ActivityOperationsService
) : AndroidPermissionsService, ListenableService<PermissionEvent> by delegate {

    private val foregroundLocationSuspendedCoroutines =
        mutableListOf<CancellableContinuation<Unit>>()
    private val backgroundLocationSuspendedCoroutines =
        mutableListOf<CancellableContinuation<Unit>>()
    private val notificationsSuspendedCoroutines =
        mutableListOf<CancellableContinuation<Unit>>()
    private val cameraSuspendedCoroutines =
        mutableListOf<CancellableContinuation<Unit>>()
    private val bluetoothSuspendedCoroutines =
        mutableListOf<CancellableContinuation<Unit>>()


    private var hasLocationPermission: Boolean

    init {
        hasLocationPermission = hasForegroundLocationPermission()
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityResumed(activity: Activity) {
                super.onActivityResumed(activity)
                checkLocationPermissions()
            }
        })
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                application.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!hasLocationPermission) {
                hasLocationPermission = true
                freeForegroundLocationCoroutines()
                delegate(PermissionEvent.LocationPermissionChanged(hasLocationPermission))
            }
        }
    }

    override fun usePermission(permission: String, job: (Boolean) -> Unit) {
        activityOperationsService.usePermission(permission, job)
    }

    override fun hasForegroundLocationPermission(): Boolean = with(
        ContextCompat.checkSelfPermission(
            application.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        if (this) {
            freeForegroundLocationCoroutines()
        }
        this
    }

    override fun hasBackgroundLocationPermission(): Boolean = with(
        ContextCompat.checkSelfPermission(
            application.applicationContext,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        if (this) {
            freeBackgroundLocationCoroutines()
        }
        this
    }

    override fun hasNotificationsPermission(): Boolean = with(
        ContextCompat.checkSelfPermission(
            application.applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        if (this) {
            freeNotificationsCoroutines()
        }
        this
    }

    override fun hasUwbRangingPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application.applicationContext,
            Manifest.permission.UWB_RANGING
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application.applicationContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application.applicationContext,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_CONNECT
            } else {
                Manifest.permission.BLUETOOTH
            },
        ) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
                        hasForegroundLocationPermission()) || ContextCompat.checkSelfPermission(
            application.applicationContext,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestForegroundLocationPermission(): Boolean =
        suspendCancellableCoroutine { continuation: CancellableContinuation<Boolean> ->
            if (hasForegroundLocationPermission()) {
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }
            activityOperationsService.usePermissions(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) {
                if (it) {
                    freeForegroundLocationCoroutines()
                    delegate(PermissionEvent.LocationPermissionChanged(true))
                }
                if (continuation.isActive) {
                    continuation.resume(it)
                }
            }
            continuation.invokeOnCancellation {}
        }

    override suspend fun requestBackgroundLocationPermission(): Boolean {
        val hasBackgroundLocation = hasBackgroundLocationPermission()
        if (hasBackgroundLocation) {
            return true
        }
        val hasPermission =
            activityOperationsService.requestPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (hasPermission) {
            freeBackgroundLocationCoroutines()
            delegate(PermissionEvent.LocationPermissionChanged(true))
        }
        return hasPermission
    }

    override suspend fun requestNotificationsPermission(): Boolean {
        if (hasNotificationsPermission()) {
            return true
        }
        val hasPermission =
            activityOperationsService.requestPermission(Manifest.permission.POST_NOTIFICATIONS)
        if (hasPermission) {
            freeNotificationsCoroutines()
            delegate(PermissionEvent.NotificationsPermissionChanged(true))
        }
        return hasPermission
    }

    override suspend fun requestCameraPermission(): Boolean {
        if (hasCameraPermission()) {
            return true
        }
        val hasPermission =
            activityOperationsService.requestPermission(Manifest.permission.CAMERA)
        if (hasPermission) {
            freeCameraCoroutines()
            delegate(PermissionEvent.CameraPermissionChanged(true))
        }
        return hasPermission
    }

    override suspend fun requestBluetoothPermission(): Boolean {
        if (hasBluetoothPermission()) {
            return true
        }
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activityOperationsService.requestPermissions(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            activityOperationsService.requestPermissions(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
//        activityOperationsService.requestPermissions(
//            Manifest.permission.BLUETOOTH_CONNECT,
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//                Manifest.permission.BLUETOOTH_SCAN
//            } else {
//                Manifest.permission.ACCESS_FINE_LOCATION
//            },
//            Manifest.permission.BLUETOOTH
//        )
//        if (hasPermission && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//            hasPermission = requestForegroundLocationPermission()

        /*

override fun hasBluetoothPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        application.applicationContext,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH
        },
    ) == PackageManager.PERMISSION_GRANTED &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || (ContextCompat.checkSelfPermission(
                application.applicationContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        application.applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED))
         */
//        }
        if (hasPermission) {
            freeBluetoothCoroutines()
            delegate(PermissionEvent.BluetoothPermissionChanged(true))
        }
        return hasPermission
    }

    override suspend fun awaitForegroundLocationPermission() {
        if (!hasForegroundLocationPermission()) {
            return suspendCancellableCoroutine { continuation ->
                foregroundLocationSuspendedCoroutines += continuation
                continuation.invokeOnCancellation {
                    foregroundLocationSuspendedCoroutines -= continuation
                }
            }
        } else {
            freeForegroundLocationCoroutines()
        }
    }

    override suspend fun awaitBackgroundLocationPermission() {
        if (!hasBackgroundLocationPermission()) {
            return suspendCancellableCoroutine { continuation ->
                backgroundLocationSuspendedCoroutines += continuation
                continuation.invokeOnCancellation {
                    backgroundLocationSuspendedCoroutines -= continuation
                }
            }
        } else {
            freeBackgroundLocationCoroutines()
        }
    }

    override suspend fun awaitBluetoothPermission() {
        if (!hasBluetoothPermission()) {
            return suspendCancellableCoroutine { continuation ->
                bluetoothSuspendedCoroutines += continuation
                continuation.invokeOnCancellation {
                    bluetoothSuspendedCoroutines -= continuation
                }
            }
        } else {
            freeBluetoothCoroutines()
        }
    }

    private fun freeForegroundLocationCoroutines() {
        val coroutines = ArrayList(foregroundLocationSuspendedCoroutines)
        foregroundLocationSuspendedCoroutines.clear()
        coroutines.forEach {
            if (it.isActive) {
                it.resume(Unit)
            }
        }
    }

    private fun freeBackgroundLocationCoroutines() {
        val coroutines = ArrayList(backgroundLocationSuspendedCoroutines)
        backgroundLocationSuspendedCoroutines.clear()
        coroutines.forEach {
            if (it.isActive) {
                it.resume(Unit)
            }
        }
    }

    private fun freeNotificationsCoroutines() {
        val coroutines = ArrayList(notificationsSuspendedCoroutines)
        notificationsSuspendedCoroutines.clear()
        coroutines.forEach {
            if (it.isActive) {
                it.resume(Unit)
            }
        }
    }

    private fun freeCameraCoroutines() {
        val coroutines = ArrayList(cameraSuspendedCoroutines)
        cameraSuspendedCoroutines.clear()
        coroutines.forEach {
            if (it.isActive) {
                it.resume(Unit)
            }
        }
    }

    private fun freeBluetoothCoroutines() {
        val coroutines = ArrayList(bluetoothSuspendedCoroutines)
        bluetoothSuspendedCoroutines.clear()
        coroutines.forEach {
            if (it.isActive) {
                it.resume(Unit)
            }
        }
    }

}