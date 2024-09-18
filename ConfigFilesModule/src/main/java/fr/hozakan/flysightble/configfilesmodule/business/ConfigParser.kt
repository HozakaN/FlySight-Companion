package fr.hozakan.flysightble.configfilesmodule.business

import fr.hozakan.flysightble.model.ConfigFile

interface ConfigParser {
    fun parse(fileLines: List<String>): ConfigFile
}