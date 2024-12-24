package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class AlarmWindowAboveParser : ConfigItemParser() {
    override fun key(): String = "Win_Above"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            windowAbove = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().windowAbove
        )
}