package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class InitFileParser : ConfigItemParser() {
    override fun key(): String = "Init_File"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            initFile = parseLine(key(), line)
        )
}