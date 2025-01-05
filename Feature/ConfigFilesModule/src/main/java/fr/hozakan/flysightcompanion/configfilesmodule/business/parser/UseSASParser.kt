package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.framework.extension.toBoolean
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class UseSASParser : ConfigItemParser() {
    override fun key(): String = "Use_SAS"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            useSAS = parseLine(key(), line).toIntOrNull()?.toBoolean() ?: defaultConfigFile().useSAS
        )
}