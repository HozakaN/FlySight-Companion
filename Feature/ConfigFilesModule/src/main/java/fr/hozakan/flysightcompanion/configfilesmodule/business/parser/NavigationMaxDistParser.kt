package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Navigation

class NavigationMaxDistParser : ConfigItemParser() {
    override fun key(): String = "Max_Dist"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val maxDist = parseLine(key(), line).toIntOrNull() ?: 10000
        val navigation = configFile.navigation.copy(maxDist = maxDist)
        return configFile.copy(navigation = navigation)
    }
}