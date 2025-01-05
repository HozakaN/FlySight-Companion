package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class FlatlineParser : ConfigItemParser() {
    override fun key(): String = "Flatline"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            flatLineAtMinimumRate = (parseLine(key(), line).toIntOrNull() ?: 0) == 1
        )
}