package fr.hozakan.flysightble.configfilesmodule.business

import fr.hozakan.flysightble.configfilesmodule.business.parser.AlarmParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.AlarmWindowAboveParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.AlarmWindowBelowParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.AltitudeStepParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.AltitudeUnitParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.DynamicModelParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.DzElevParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.CONFIG_NAME_INDICATOR
import fr.hozakan.flysightble.configfilesmodule.business.parser.FlatlineParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.HorizontalThresholdParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.InitFileParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.InitModeParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.MultilineConfigItemParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.RateMaximumParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.RateMaximumValueParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.RateMinimumParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.RateMinimumValueParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.RateModeParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.SamplePeriodParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.SilenceWindowParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.SpeechParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.SpeechRateParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.SpeechVolumeParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.TimeZoneOffsetParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.ToneLimitBehaviourParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.ToneMaximumParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.ToneMinimumParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.ToneModeParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.ToneVolumeParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.UseSASParser
import fr.hozakan.flysightble.configfilesmodule.business.parser.VerticalThresholdParser
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile

class DefaultConfigParser : ConfigParser {
    override fun parse(fileLines: List<String>): ConfigFile {

        fun parseFileName(line: String): String? {
            if (line.contains(CONFIG_NAME_INDICATOR)) {line.substringAfter(CONFIG_NAME_INDICATOR).trim().run {
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
    SamplePeriodParser(),
    ToneModeParser(),
    ToneMinimumParser(),
    ToneMaximumParser(),
    ToneLimitBehaviourParser(),
    ToneVolumeParser(),
    RateModeParser(),
    RateMinimumValueParser(),
    RateMaximumValueParser(),
    RateMinimumParser(),
    RateMaximumParser(),
    FlatlineParser(),
    SpeechRateParser(),
    SpeechVolumeParser(),
    SpeechParser(),
    VerticalThresholdParser(),
    HorizontalThresholdParser(),
    UseSASParser(),
    TimeZoneOffsetParser(),
    InitModeParser(),
    InitFileParser(),
    AlarmWindowAboveParser(),
    AlarmWindowBelowParser(),
    DzElevParser(),
    AlarmParser(),
    AltitudeUnitParser(),
    AltitudeStepParser(),
    SilenceWindowParser()
)