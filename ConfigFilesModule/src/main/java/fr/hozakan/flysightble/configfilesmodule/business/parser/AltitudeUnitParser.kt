package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.AltitudeUnit
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class AltitudeUnitParser : ConfigItemParser() {
    override fun key(): String = "Alt_Units"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            altitudeUnit = AltitudeUnit.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().altitudeUnit
        )
}