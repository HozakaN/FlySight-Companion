package fr.hozakan.flysightble

sealed class AppScreen(val route: String) {
    data object DeviceList : AppScreen("device_list")
    data object DeviceDetail: AppScreen("device_detail/{deviceId}") {
        fun buildRoute(deviceId: String) = "device_detail/$deviceId"
    }
}