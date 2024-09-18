package fr.hozakan.flysightble.configfilesmodule.business.parser

import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigParser
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.SpeechMode
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

}