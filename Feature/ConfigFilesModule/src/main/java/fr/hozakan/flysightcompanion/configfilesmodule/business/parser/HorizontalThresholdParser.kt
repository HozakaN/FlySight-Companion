package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class HorizontalThresholdParser : ConfigItemParser() {
    override fun key(): String = "H_Thresh"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            horizontalThreshold = parseLine(key(), line).toIntOrNull()
                ?: defaultConfigFile().horizontalThreshold
        )
}