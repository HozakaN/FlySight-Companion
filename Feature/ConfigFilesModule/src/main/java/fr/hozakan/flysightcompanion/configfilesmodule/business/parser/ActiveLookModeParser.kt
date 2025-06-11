package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.ActiveLookMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ActiveLookModeParser : ConfigItemParser() {
    override fun key(): String = "AL_Mode"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            activeLook = configFile.activeLook.copy(
                mode = ActiveLookMode.fromValue(parseLine(key(), line).toIntOrNull() ?: -1) 
                    ?: defaultConfigFile().activeLook.mode
            )
        )
}