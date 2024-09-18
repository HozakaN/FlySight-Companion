package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class ToneModeParser : ConfigItemParser() {
    override fun key(): String = "Model"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            toneMode = ToneMode.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().toneMode
        )
}