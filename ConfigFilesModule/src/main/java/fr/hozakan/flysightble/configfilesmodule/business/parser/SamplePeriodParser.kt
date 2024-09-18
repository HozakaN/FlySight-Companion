package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile

class SamplePeriodParser : ConfigItemParser() {
    override fun key(): String = "Rate"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            samplePeriod = parseLine(key(), line).toIntOrNull() ?: defaultConfigFile().samplePeriod
        )
}