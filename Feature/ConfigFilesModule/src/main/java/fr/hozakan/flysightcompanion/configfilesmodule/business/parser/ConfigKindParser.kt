package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile

class ConfigKindParser : ConfigItemParser() {
    override fun key(): String = "Kind"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            group = parseLine(key(), line)
        )
}