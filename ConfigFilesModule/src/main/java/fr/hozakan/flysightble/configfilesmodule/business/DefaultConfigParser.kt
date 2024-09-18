package fr.hozakan.flysightble.configfilesmodule.business

import fr.hozakan.flysightble.configfilesmodule.business.parser.DynamicModelParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.FILENAME_INDICATOR
import fr.hozakan.flysightble.configfilesmodule.business.parser.MultilineConfigItemParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.SpeechParser
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile

class DefaultConfigParser : ConfigParser {
    override fun parse(fileLines: List<String>): ConfigFile {

        fun parseFileName(line: String): String? {
            if (line.contains(FILENAME_INDICATOR)) {line.substringAfter(FILENAME_INDICATOR).trim().run {
                    return if (contains(";")) {
                        substringBefore(";").trim()
                    } else {
                        this
                    }
                }
            }
            return null
        }

        var fileName = "unknown"

        var newConfig = defaultConfigFile()
        var multilineParser: MultilineConfigItemParser? = null
        fileLines.forEachIndexed { index, line ->
            when (index) {
                0 -> {
                    fileName = parseFileName(line) ?: fileName
                }

                else -> {
                    if (line.isBlank()) return@forEachIndexed
                    if (line.startsWith(";")) return@forEachIndexed
                    if (multilineParser != null) {
                        newConfig = multilineParser!!.fillConfigFile(line, newConfig)
                        if (multilineParser?.isSatisfied() == true) {
                            multilineParser?.reset()
                            multilineParser = null
                        }
                    } else {
                        configItemsParsers.forEach {
                            if (line.startsWith(it.key())) {
                                newConfig = it.fillConfigFile(line, newConfig)
                                if (it.isMultilineParser()) {
                                    multilineParser = it as MultilineConfigItemParser
                                }
                                return@forEach
                            }
                        }
                    }
                }
            }
        }
        return newConfig.copy(name = fileName)
    }
}

private val configItemsParsers = listOf(
    DynamicModelParser(),
    SpeechParser()
)