package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ToneLimitBehaviourParser : ConfigItemParser() {
    override fun key(): String = "Limits"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            toneLimitBehaviour = ToneLimitBehaviour.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().toneLimitBehaviour
        )
}