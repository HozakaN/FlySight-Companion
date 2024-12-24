package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class RateMinimumValueParser : ConfigItemParser() {
    override fun key(): String = "Min_Val_2"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            rateMinimumValue = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().rateMinimumValue
        )
}