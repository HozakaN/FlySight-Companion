package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ConfigNameParser : ConfigItemParser() {
    override fun key(): String = "Name"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            name = parseLine(key(), line)
        )
}