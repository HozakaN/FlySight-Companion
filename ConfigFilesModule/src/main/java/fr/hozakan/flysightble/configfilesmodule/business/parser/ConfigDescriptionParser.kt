package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.InitMode
import fr.hozakan.flysightble.model.defaultConfigFile

class ConfigDescriptionParser : ConfigItemParser() {
    override fun key(): String = "Description"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            description = parseLine(key(), line)
        )
}