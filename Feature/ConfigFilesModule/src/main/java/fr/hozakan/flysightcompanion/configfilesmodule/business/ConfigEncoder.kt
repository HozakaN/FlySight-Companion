package fr.hozakan.flysightcompanion.configfilesmodule.business

import fr.hozakan.flysightcompanion.model.ConfigFile

interface ConfigEncoder {
    fun encodeConfig(configFile: ConfigFile): String
}