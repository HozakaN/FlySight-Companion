package fr.hozakan.flysightble.configfilesmodule.business.encoder

import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigEncoder
import fr.hozakan.flysightble.configfilesmodule.business.parser.CONFIG_NAME_INDICATOR
import fr.hozakan.flysightble.model.config.Speech
import fr.hozakan.flysightble.model.config.SpeechMode
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
            speeches = listOf(
                Speech(
                    mode = SpeechMode.VerticalSpeed,
                    unit = UnitSystem.Imperial,
                    value = 100
                )
            )
        )
        val encoded = encore.encodeConfig(config)

        //TODO check git and speeches addition
        Assert.assertTrue(encoded.contains("${CONFIG_NAME_INDICATOR}test"))
        Assert.assertTrue(encoded.contains("Sp_Mode:        1"))
    }

}