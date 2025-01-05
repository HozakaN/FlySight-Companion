package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class RateMaximumValueParser : ConfigItemParser() {
    override fun key(): String = "Max_Val_2"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            rateMaximumValue = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().rateMaximumValue
        )
}