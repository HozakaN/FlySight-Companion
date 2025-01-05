package fr.hozakan.flysightcompanion.configfilesmodule.business.encoder

import fr.hozakan.flysightcompanion.model.ConfigFile

abstract class ConfigItemEncoder {
    abstract fun encode(sb: StringBuilder, configFile: ConfigFile)
}