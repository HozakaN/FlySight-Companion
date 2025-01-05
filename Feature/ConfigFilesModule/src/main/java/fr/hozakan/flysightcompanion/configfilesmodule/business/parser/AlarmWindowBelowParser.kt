package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class AlarmWindowBelowParser : ConfigItemParser() {
    override fun key(): String = "Win_Below"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            windowBelow = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().windowBelow
        )
}