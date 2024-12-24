package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ToneMinimumParser : ConfigItemParser() {
    override fun key(): String = "Min"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            toneMinimum = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().toneMinimum
        )
}