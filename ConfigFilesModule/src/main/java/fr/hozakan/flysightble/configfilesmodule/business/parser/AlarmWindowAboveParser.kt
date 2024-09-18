package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class AlarmWindowAboveParser : ConfigItemParser() {
    override fun key(): String = "Win_Above"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            windowAbove = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().windowAbove
        )
}