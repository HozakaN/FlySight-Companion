package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.InitMode
import fr.hozakan.flysightble.model.defaultConfigFile

class ConfigNameParser : ConfigItemParser() {
    override fun key(): String = "Name"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            name = parseLine(key(), line)
        )
}