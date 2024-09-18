package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class SpeechRateParser : ConfigItemParser() {
    override fun key(): String = "Sp_Rate"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            speechRate = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().speechRate
        )
}