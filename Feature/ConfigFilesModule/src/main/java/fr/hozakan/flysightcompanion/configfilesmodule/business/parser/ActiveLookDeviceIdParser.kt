package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class ActiveLookDeviceIdParser : ConfigItemParser() {
    override fun key(): String = "AL_ID"
    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile =
        configFile.copy(
            activeLook = configFile.activeLook.copy(
                deviceId = parseLine(key(), line).ifBlank { defaultConfigFile().activeLook.deviceId }
            )
        )
}