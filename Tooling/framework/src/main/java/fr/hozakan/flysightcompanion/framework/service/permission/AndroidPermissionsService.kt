package fr.hozakan.flysightcompanion.framework.service.permission

import fr.hozakan.flysightcompanion.framework.service.ListenableService

interface AndroidPermissionsService : ListenableService<PermissionEvent> {
    fun usePermission(permission: String, job: (Boolean) -> Unit)
    fun hasForegroundLocationPermission(): Boolean
    fun hasBackgroundLocationPermission(): Boolean
    fun hasNotificationsPermission(): Boolean
    fun hasUwbRangingPermission(): Boolean
    fun hasCameraPermission(): Boolean
    fun hasBluetoothPermission(): Boolean
    suspend fun requestForegroundLocationPermission(): Boolean
    suspend fun requestBackgroundLocationPermission(): Boolean
    suspend fun requestNotificationsPermission(): Boolean
    suspend fun requestCameraPermission(): Boolean
    suspend fun requestBluetoothPermission(): Boolean
    suspend fun awaitForegroundLocationPermission()
    suspend fun awaitBackgroundLocationPermission()
    suspend fun awaitBluetoothPermission()
}