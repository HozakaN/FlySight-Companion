package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.configfilesmodule.business.DefaultConfigParser
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.Volume
import org.junit.Assert
import org.junit.Test

class DefaultConfigParserTests {

    @Test
    fun `test ConfigurationParser nominal behaviour`() {
        val parser = DefaultConfigParser()

        val fileLines = listOf(
            ";.fsmp_name_my_config_file",
            "Model: 2",
            "Sp_Mode: 2",
            "Sp_Units: 1",
            "Sp_Dec: 1"
        )
        val configFile = parser.parse(fileLines)

        Assert.assertTrue(configFile.name == "my_config_file")
        Assert.assertTrue(configFile.dynamicModel == DynamicModel.Stationary)
        Assert.assertTrue(configFile.speeches.size == 1)
        val speech = configFile.speeches[0]
        Assert.assertEquals(SpeechMode.GlideRatio, speech.mode)
        Assert.assertEquals(1, speech.value)
    }

    @Test
    fun `test ConfigurationParser when lines end with comments`() {
        val parser = DefaultConfigParser()

        val fileLines = listOf(
            ";.fsmp_name_my_config_file ; hello world",
            "Model: 2 ; add some comment",
            "Sp_Mode: 3 ; some other comments",
            "Sp_Units: 1",
            "Sp_Dec: 2 ; comments again"
        )
        val configFile = parser.parse(fileLines)

        Assert.assertTrue(configFile.name == "my_config_file")
        Assert.assertTrue(configFile.dynamicModel == DynamicModel.Stationary)
        Assert.assertTrue(configFile.speeches.size == 1)
        val speech = configFile.speeches[0]
        Assert.assertEquals(SpeechMode.InverseGlideRatio, speech.mode)
        Assert.assertEquals(2, speech.value)
    }

    @Test
    fun `test Beaufort distance config nominal behaviour`() {
        val parser = DefaultConfigParser()

        val fileContent = javaClass.classLoader
            ?.getResource("CONFIG.TXT")?.readText()
        Assert.assertTrue(!fileContent.isNullOrBlank())
        if (fileContent == null) {
            return
        }
        val fileLines = fileContent.lines()
        val configFile = parser.parse(fileLines)

        Assert.assertEquals(DynamicModel.Airborne2g, configFile.dynamicModel)
        Assert.assertEquals(200, configFile.samplePeriod)

        Assert.assertEquals(ToneMode.GlideRatio, configFile.toneMode)
        Assert.assertEquals(0, configFile.toneMinimum)
        Assert.assertEquals(300, configFile.toneMaximum)
        Assert.assertEquals(ToneLimitBehaviour.MinMaxTone, configFile.toneLimitBehaviour)
        Assert.assertEquals(Volume.Volume0, configFile.toneVolume)

        Assert.assertEquals(RateMode.ChangeInValue1, configFile.rateMode)
        Assert.assertEquals(300, configFile.rateMinimumValue)
        Assert.assertEquals(1500, configFile.rateMaximumValue)
        Assert.assertEquals(0, configFile.rateMinimum)
        Assert.assertEquals(0, configFile.rateMaximum)
        Assert.assertFalse(configFile.flatLineAtMinimumRate)

        Assert.assertEquals(5, configFile.speechRate)
        Assert.assertEquals(Volume.Volume8, configFile.speechVolume)
        Assert.assertEquals(1, configFile.speeches.size)
        val speech = configFile.speeches[0]
        Assert.assertEquals(SpeechMode.VerticalSpeed, speech.mode)
        Assert.assertEquals(0, speech.value)

        Assert.assertEquals(1000, configFile.verticalThreshold)
        Assert.assertEquals(0, configFile.horizontalThreshold)

        Assert.assertTrue(configFile.useSAS)
        Assert.assertEquals(0, configFile.tzOffset)

        Assert.assertEquals(InitMode.PlayFile, configFile.initMode)
        Assert.assertEquals("distance", configFile.initFile)

        Assert.assertEquals(50, configFile.windowAbove)
        Assert.assertEquals(30, configFile.windowBelow)
        Assert.assertEquals(3, configFile.dzElev)

        Assert.assertEquals(10, configFile.alarms.size)
        val alarm0 = configFile.alarms[0]
        Assert.assertEquals(3500, alarm0.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm0.alarmType)
        Assert.assertEquals("base", alarm0.alarmFile)

        val alarm1 = configFile.alarms[1]
        Assert.assertEquals(3300, alarm1.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm1.alarmType)
        Assert.assertEquals("dive", alarm1.alarmFile)

        val alarm2 = configFile.alarms[2]
        Assert.assertEquals(2800, alarm2.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm2.alarmType)
        Assert.assertEquals("1", alarm2.alarmFile)

        val alarm3 = configFile.alarms[3]
        Assert.assertEquals(2700, alarm3.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm3.alarmType)
        Assert.assertEquals("2", alarm3.alarmFile)

        val alarm4 = configFile.alarms[4]
        Assert.assertEquals(2600, alarm4.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm4.alarmType)
        Assert.assertEquals("3", alarm4.alarmFile)

        val alarm5 = configFile.alarms[5]
        Assert.assertEquals(2500, alarm5.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm5.alarmType)
        Assert.assertEquals("base", alarm5.alarmFile)

        val alarm6 = configFile.alarms[6]
        Assert.assertEquals(1650, alarm6.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm6.alarmType)
        Assert.assertEquals("1", alarm6.alarmFile)

        val alarm7 = configFile.alarms[7]
        Assert.assertEquals(1600, alarm7.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm7.alarmType)
        Assert.assertEquals("2", alarm7.alarmFile)

        val alarm8 = configFile.alarms[8]
        Assert.assertEquals(1550, alarm8.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm8.alarmType)
        Assert.assertEquals("3", alarm8.alarmFile)

        val alarm9 = configFile.alarms[9]
        Assert.assertEquals(1500, alarm9.alarmElevation)
        Assert.assertEquals(AlarmType.PlayFile, alarm9.alarmType)
        Assert.assertEquals("base", alarm9.alarmFile)

        Assert.assertEquals(0, configFile.altitudeUnit.value)
        Assert.assertEquals(0, configFile.altitudeStep)

        Assert.assertEquals(2, configFile.silenceWindows.size)
        val silenceWindow0 = configFile.silenceWindows[0]
        Assert.assertEquals(1500, silenceWindow0.top)
        Assert.assertEquals(0, silenceWindow0.bottom)

        val silenceWindow1 = configFile.silenceWindows[1]
        Assert.assertEquals(4500, silenceWindow1.top)
        Assert.assertEquals(2500, silenceWindow1.bottom)
    }

}
