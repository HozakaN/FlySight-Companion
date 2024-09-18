package fr.hozakan.flysightble.configfilesmodule.business.encoder

import fr.hozakan.flysightble.model.ConfigFile

abstract class ConfigItemEncoder {
    abstract fun encode(sb: StringBuilder, configFile: ConfigFile)
}