package fr.hozakan.flysightcompanion.framework.service.permission

sealed class PermissionEvent {
    data class NotificationsPermissionChanged(val newValue: Boolean) : PermissionEvent()
    data class LocationPermissionChanged(val newValue: Boolean) : PermissionEvent()
    data class ForegroundServicePermissionChanged(val newValue: Boolean) : PermissionEvent()
    data class FilePermissionChanged(val newValue: Boolean) : PermissionEvent()
    data class CameraPermissionChanged(val newValue: Boolean) : PermissionEvent()
    data class BluetoothPermissionChanged(val newValue: Boolean) : PermissionEvent()
}