package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class TimeZoneOffsetParser : ConfigItemParser() {
    override fun key(): String = "TZ_Offset"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            tzOffset = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().tzOffset
        )
}