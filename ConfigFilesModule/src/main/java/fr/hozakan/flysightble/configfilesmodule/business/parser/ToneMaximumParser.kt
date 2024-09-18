package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class ToneMaximumParser : ConfigItemParser() {
    override fun key(): String = "Max"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            toneMaximum = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().toneMaximum
        )
}