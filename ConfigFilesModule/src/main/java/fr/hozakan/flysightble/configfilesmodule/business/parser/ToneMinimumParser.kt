package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class ToneMinimumParser : ConfigItemParser() {
    override fun key(): String = "Min"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            toneMinimum = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().toneMinimum
        )
}