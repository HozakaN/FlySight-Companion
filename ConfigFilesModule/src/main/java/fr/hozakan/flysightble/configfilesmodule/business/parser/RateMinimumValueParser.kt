package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class RateMinimumValueParser : ConfigItemParser() {
    override fun key(): String = "Min_Val_2"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            rateMinimumValue = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().rateMinimumValue
        )
}