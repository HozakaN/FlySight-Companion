package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class AltitudeStepParser : ConfigItemParser() {
    override fun key(): String = "Alt_Step"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            altitudeStep = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().altitudeStep
        )
}