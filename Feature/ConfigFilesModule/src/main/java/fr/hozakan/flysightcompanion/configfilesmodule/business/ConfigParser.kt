package fr.hozakan.flysightcompanion.configfilesmodule.business

import fr.hozakan.flysightcompanion.model.ConfigFile

interface ConfigParser {
    fun parse(fileLines: List<String>): ConfigFile
}