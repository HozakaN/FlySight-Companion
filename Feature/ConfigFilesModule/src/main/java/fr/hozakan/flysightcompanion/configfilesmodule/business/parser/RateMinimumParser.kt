package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class RateMinimumParser : ConfigItemParser() {
    override fun key(): String = "Min_Rate"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            rateMinimum = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().rateMinimum
        )
}