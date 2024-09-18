package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.RateMode
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class RateModeParser : ConfigItemParser() {
    override fun key(): String = "Mode_2"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            rateMode = RateMode.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().rateMode
        )
}