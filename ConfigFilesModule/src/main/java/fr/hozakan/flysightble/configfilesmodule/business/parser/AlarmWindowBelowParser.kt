package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class AlarmWindowBelowParser : ConfigItemParser() {
    override fun key(): String = "Win_Below"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            windowBelow = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().windowBelow
        )
}