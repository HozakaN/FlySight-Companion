package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Navigation

class NavigationEndNavParser : ConfigItemParser() {
    override fun key(): String = "End_Nav"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val endNav = parseLine(key(), line).toIntOrNull() ?: 1500
        val navigation = configFile.navigation.copy(endNav = endNav)
        return configFile.copy(navigation = navigation)
    }
}