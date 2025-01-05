package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class DynamicModelParser : ConfigItemParser() {
    override fun key(): String = "Model"

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        val strValue = parseLine(key(), line)
        val value = strValue.toIntOrNull() ?: -1
        val fromValue = DynamicModel.fromValue(value)
        val copy = configFile.copy(
            dynamicModel = fromValue
                ?: defaultConfigFile().dynamicModel
        )
        return copy
    }
}