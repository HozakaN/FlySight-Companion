package fr.hozakan.flysightcompanion.configfilesmodule.business.encoder

import fr.hozakan.flysightcompanion.configfilesmodule.business.DefaultConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.CONFIG_NAME_INDICATOR
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.defaultConfigFile
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