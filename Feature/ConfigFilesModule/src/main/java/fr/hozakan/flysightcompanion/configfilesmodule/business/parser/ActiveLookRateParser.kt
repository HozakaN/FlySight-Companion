package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ActiveLookRateParser : ConfigItemParser() {
    override fun key(): String = "AL_Rate"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            activeLook = configFile.activeLook.copy(
                rate = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().activeLook.rate
            )
        )
}