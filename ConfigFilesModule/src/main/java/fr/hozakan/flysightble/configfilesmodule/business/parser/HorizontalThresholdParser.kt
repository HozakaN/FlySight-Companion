package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile

class HorizontalThresholdParser : ConfigItemParser() {
    override fun key(): String = "H_Thresh"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            horizontalThreshold = parseLine(key(), line).toIntOrNull()
                ?: defaultConfigFile().horizontalThreshold
        )
}