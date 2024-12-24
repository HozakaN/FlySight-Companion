package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class RateModeParser : ConfigItemParser() {
    override fun key(): String = "Mode_2"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            rateMode = RateMode.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().rateMode
        )
}