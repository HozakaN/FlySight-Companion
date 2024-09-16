package fr.hozakan.flysightble.model

sealed interface DeviceConnectionState {
    data object Connecting : DeviceConnectionState
    data object Connected : DeviceConnectionState
    data object Disconnected : DeviceConnectionState
    data object ConnectionError : DeviceConnectionState
}