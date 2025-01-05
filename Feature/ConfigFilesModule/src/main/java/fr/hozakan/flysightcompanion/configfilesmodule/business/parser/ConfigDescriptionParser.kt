package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ConfigDescriptionParser : ConfigItemParser() {
    override fun key(): String = "Description"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            description = parseLine(key(), line)
        )
}