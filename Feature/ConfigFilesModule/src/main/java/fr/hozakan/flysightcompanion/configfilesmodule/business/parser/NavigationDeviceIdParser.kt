package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Navigation

class NavigationDeviceIdParser : ConfigItemParser() {
    override fun key(): String = "Device_ID"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val deviceId = parseLine(key(), line)
        val navigation = configFile.navigation.copy(deviceId = deviceId)
        return configFile.copy(navigation = navigation)
    }
}