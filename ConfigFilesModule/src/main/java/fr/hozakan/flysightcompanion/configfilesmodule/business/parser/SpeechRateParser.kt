package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class SpeechRateParser : ConfigItemParser() {
    override fun key(): String = "Sp_Rate"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            speechRate = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().speechRate
        )
}