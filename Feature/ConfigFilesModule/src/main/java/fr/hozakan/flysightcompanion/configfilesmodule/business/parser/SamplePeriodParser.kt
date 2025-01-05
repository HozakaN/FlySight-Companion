package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class SamplePeriodParser : ConfigItemParser() {
    override fun key(): String = "Rate"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            samplePeriod = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().samplePeriod
        )
}