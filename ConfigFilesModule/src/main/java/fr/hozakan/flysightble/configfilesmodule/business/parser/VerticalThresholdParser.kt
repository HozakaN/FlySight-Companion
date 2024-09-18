package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class VerticalThresholdParser : ConfigItemParser() {
    override fun key(): String = "V_Thresh"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            verticalThreshold = parseLine(key(), line).toIntOrNull()?.times(0.036)?.toInt()
                ?: defaultConfigFile().verticalThreshold
        )
}