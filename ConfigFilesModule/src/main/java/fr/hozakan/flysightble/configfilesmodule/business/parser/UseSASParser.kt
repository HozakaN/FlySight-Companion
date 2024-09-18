package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.framework.extension.toBoolean
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile

class UseSASParser : ConfigItemParser() {
    override fun key(): String = "Use_SAS"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            useSAS = parseLine(key(), line).toIntOrNull()?.toBoolean() ?: defaultConfigFile().useSAS
        )
}