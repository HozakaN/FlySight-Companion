package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class DzElevParser : ConfigItemParser() {
    override fun key(): String = "Dz_Elev"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            dzElev = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().dzElev
        )
}