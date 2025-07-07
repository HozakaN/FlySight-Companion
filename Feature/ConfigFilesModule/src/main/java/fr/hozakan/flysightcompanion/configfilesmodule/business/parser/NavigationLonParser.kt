package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Navigation

class NavigationLonParser : ConfigItemParser() {
    override fun key(): String = "Lon"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val lon = parseLine(key(), line).toIntOrNull() ?: 0
        val navigation = configFile.navigation.copy(lon = lon)
        return configFile.copy(navigation = navigation)
    }
}