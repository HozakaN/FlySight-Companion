package fr.hozakan.flysightble.configfilesmodule.business.encoder

import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigEncoder
import fr.hozakan.flysightble.configfilesmodule.business.parser.FILENAME_INDICATOR
import fr.hozakan.flysightble.model.config.Speech
import fr.hozakan.flysightble.model.config.SpeechMode
import fr.hozakan.flysightble.model.config.SpeechUnit
import fr.hozakan.flysightble.model.config.UnitSystem
import fr.hozakan.flysightble.model.defaultConfigFile
import org.junit.Assert
import org.junit.Test

class DefaultConfigEncoderTests {

    @Test
    fun `test encoder nominal behaviour`() {
        val encore = DefaultConfigEncoder()
        val config = defaultConfigFile().copy(
            name = "test",
            unitSystem = UnitSystem.Imperial,
            speeches = listOf(
                Speech(
                    mode = SpeechMode.VerticalSpeed,
                    unit = SpeechUnit.Imperial,
                    value = 100
                )
            )
        )
        val encoded = encore.encoreConfig(config)

        //TODO check git and speeches addition
        Assert.assertTrue(encoded.contains("${FILENAME_INDICATOR}test"))
        Assert.assertTrue(encoded.contains("Sp_Mode:        1"))
    }

}