package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Navigation

class NavigationLatParser : ConfigItemParser() {
    override fun key(): String = "Lat"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val lat = parseLine(key(), line).toIntOrNull() ?: 0
        val navigation = configFile.navigation.copy(lat = lat)
        return configFile.copy(navigation = navigation)
    }
}