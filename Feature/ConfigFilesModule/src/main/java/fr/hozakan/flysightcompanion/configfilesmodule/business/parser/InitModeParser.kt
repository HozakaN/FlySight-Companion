package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class InitModeParser : ConfigItemParser() {
    override fun key(): String = "Init_Mode"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            initMode = InitMode.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().initMode
        )
}