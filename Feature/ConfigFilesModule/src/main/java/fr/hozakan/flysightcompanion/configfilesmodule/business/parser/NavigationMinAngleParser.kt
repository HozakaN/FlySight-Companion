package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Navigation

class NavigationMinAngleParser : ConfigItemParser() {
    override fun key(): String = "Min_Angle"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val minAngle = parseLine(key(), line).toIntOrNull() ?: 5
        val navigation = configFile.navigation.copy(minAngle = minAngle)
        return configFile.copy(navigation = navigation)
    }
}