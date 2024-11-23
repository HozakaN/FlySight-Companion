package fr.hozakan.flysightble

sealed class AppScreen(val route: String) {
    data object Device : AppScreen("device") {
        data object DeviceList : AppScreen("device_list")
        data object DeviceDetail: AppScreen("device_detail/{deviceId}") {
            fun buildRoute(deviceId: String) = "device_detail/$deviceId"
        }
        data object DeviceFile : AppScreen("device_file/{deviceId}/{filePath}") {
            fun buildRoute(deviceId: String, filePath: List<String>) = "device_file/$deviceId/${filePath.subList(1, filePath.size).joinToString(";")}"
//            fun buildRoute(deviceId: String, filePath: List<String>) = "device_file/$deviceId/$deviceId"
        }
        data object DeviceConfig : AppScreen("device_config/{config}") {
            fun buildRoute(config: String) = "device_config/$config"
        }
    }
    data object ConfigFiles : AppScreen("config_files") {
        data object ConfigFileList : AppScreen("config_file_list")
        data object ConfigFileDetail: AppScreen("config_file_detail/{configFileName}") {
            fun buildRoute(configFileName: String) = "config_file_detail/$configFileName"
        }
    }
}