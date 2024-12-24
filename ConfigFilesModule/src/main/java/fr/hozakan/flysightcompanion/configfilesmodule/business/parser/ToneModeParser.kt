package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ToneModeParser : ConfigItemParser() {
    override fun key(): String = "Mode"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            toneMode = ToneMode.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().toneMode
        )
}