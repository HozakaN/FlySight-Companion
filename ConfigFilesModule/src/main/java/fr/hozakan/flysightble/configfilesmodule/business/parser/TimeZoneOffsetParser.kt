package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.defaultConfigFile

class TimeZoneOffsetParser : ConfigItemParser() {
    override fun key(): String = "TZ_Offset"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            tzOffset = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().tzOffset
        )
}