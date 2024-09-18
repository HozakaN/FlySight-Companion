package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class RateMaximumValueParser : ConfigItemParser() {
    override fun key(): String = "Max_Val_2"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            rateMaximumValue = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().rateMaximumValue
        )
}