package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Navigation

class NavigationBearingParser : ConfigItemParser() {
    override fun key(): String = "Bearing"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val bearing = parseLine(key(), line).toIntOrNull() ?: 0
        val navigation = configFile.navigation.copy(bearing = bearing)
        return configFile.copy(navigation = navigation)
    }
}