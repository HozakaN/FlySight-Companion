package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.Volume
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class SpeechVolumeParser : ConfigItemParser() {
    override fun key(): String = "Sp_Volume"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            speechVolume = Volume.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().speechVolume
        )
}