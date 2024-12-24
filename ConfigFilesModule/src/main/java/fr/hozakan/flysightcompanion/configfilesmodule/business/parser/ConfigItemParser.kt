package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile

abstract class ConfigItemParser {
    fun parseLine(key: String, line: String): String {
        val sbstr = line.substringAfter("${key}:").trim()
        return if (sbstr.contains(";")) {
            sbstr.substringBefore(";").trim()
        } else {
            sbstr
        }
    }
    abstract fun key(): String
    abstract fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile
    open fun isMultilineParser(): Boolean = false
}