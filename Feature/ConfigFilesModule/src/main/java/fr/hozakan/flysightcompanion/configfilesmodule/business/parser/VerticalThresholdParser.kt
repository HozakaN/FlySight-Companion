package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class VerticalThresholdParser : ConfigItemParser() {
    override fun key(): String = "V_Thresh"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            verticalThreshold = parseLine(key(), line).toIntOrNull()
                ?: defaultConfigFile().verticalThreshold
        )
}