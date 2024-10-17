package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile

class VerticalThresholdParser : ConfigItemParser() {
    override fun key(): String = "V_Thresh"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            verticalThreshold = parseLine(key(), line).toIntOrNull()
                ?: defaultConfigFile().verticalThreshold
        )
}