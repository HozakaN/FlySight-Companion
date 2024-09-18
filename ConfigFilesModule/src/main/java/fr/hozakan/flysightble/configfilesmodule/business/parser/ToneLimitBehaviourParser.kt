package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.ToneLimitBehaviour
import fr.hozakan.flysightble.model.defaultConfigFile

class ToneLimitBehaviourParser : ConfigItemParser() {
    override fun key(): String = "Limits"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            toneLimitBehaviour = ToneLimitBehaviour.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().toneLimitBehaviour
        )
}