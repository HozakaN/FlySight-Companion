package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.config.Volume
import fr.hozakan.flysightble.model.defaultConfigFile

class SpeechVolumeParser : ConfigItemParser() {
    override fun key(): String = "Sp_Volume"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            speechVolume = Volume.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().speechVolume
        )
}