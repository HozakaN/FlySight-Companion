package fr.hozakan.flysightble.configfilesmodule.business

import fr.hozakan.flysightble.model.ConfigFile

interface ConfigEncoder {
    fun encoreConfig(configFile: ConfigFile): String
}