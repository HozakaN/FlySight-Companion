package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class AltitudeUnitParser : ConfigItemParser() {
    override fun key(): String = "Alt_Units"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            altitudeUnit = UnitSystem.fromValue(parseLine(key(), line).toIntOrNull() ?: -1)
                ?: defaultConfigFile().altitudeUnit
        )
}