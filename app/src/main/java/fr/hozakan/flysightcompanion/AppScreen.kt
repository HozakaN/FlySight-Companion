package fr.hozakan.flysightcompanion

sealed class AppScreen(val route: String) {
    data object DeviceTab : AppScreen("device_tab") {
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
    data object ConfigTab : AppScreen("config_tab") {
        data object ConfigList : AppScreen("config_list")
        data object ConfigDetail: AppScreen("config_detail/{configName}") {
            fun buildRoute(configName: String) = "config_detail/$configName"
        }
    }
    data object RecordTab : AppScreen("record_tab") {
        data object RecordList : AppScreen("record_list")
        data object PlotSettings : AppScreen("plot_settings")
        data object RecordDetail: AppScreen("record_detail/{recordName}") {
            fun buildRoute(recordName: String) = "record_detail/$recordName"
        }
    }
}