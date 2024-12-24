package fr.hozakan.flysightcompanion.configfilesmodule.business

import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.AlarmParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.AlarmWindowAboveParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.AlarmWindowBelowParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.AltitudeStepParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.AltitudeUnitParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.DynamicModelParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.DzElevParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.CONFIG_NAME_INDICATOR
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ConfigDescriptionParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ConfigKindParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ConfigNameParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.FlatlineParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.HorizontalThresholdParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.InitFileParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.InitModeParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.MultilineConfigItemParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.RateMaximumParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.RateMaximumValueParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.RateMinimumParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.RateMinimumValueParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.RateModeParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.SamplePeriodParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.SilenceWindowParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.SpeechParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.SpeechRateParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.SpeechVolumeParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.TimeZoneOffsetParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ToneLimitBehaviourParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ToneMaximumParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ToneMinimumParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ToneModeParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.ToneVolumeParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.UseSASParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.VerticalThresholdParser
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

class DefaultConfigParser : ConfigParser {
    override fun parse(fileLines: List<String>): ConfigFile {

        var newConfig = defaultConfigFile()
        var multilineParser: MultilineConfigItemParser? = null
        fileLines.forEachIndexed { index, line ->
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
        return newConfig
    }
}

private val configItemsParsers = listOf(
    ConfigNameParser(),
    ConfigDescriptionParser(),
    ConfigKindParser(),
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