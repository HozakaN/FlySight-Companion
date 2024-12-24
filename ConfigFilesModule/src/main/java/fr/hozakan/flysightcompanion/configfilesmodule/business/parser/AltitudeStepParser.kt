package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class AltitudeStepParser : ConfigItemParser() {
    override fun key(): String = "Alt_Step"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            altitudeStep = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().altitudeStep
        )
}