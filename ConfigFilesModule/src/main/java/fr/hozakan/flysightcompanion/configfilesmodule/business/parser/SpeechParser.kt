package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem

class SpeechParser : MultilineConfigItemParser() {

    private var speechMode: SpeechMode? = null
    private var speechUnit: UnitSystem? = null
    private var speechDecimal: Int? = null

    override fun configItemFilled(): Boolean {
        return speechMode != null && speechUnit != null && speechDecimal != null
    }

    override fun key(): String {
        return "Sp_Mode"
    }

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        if (line.startsWith("Sp_Mode")) {
            speechMode = SpeechMode.fromValue(parseLine("Sp_Mode", line).toIntOrNull() ?: -1)
        } else if (line.startsWith("Sp_Units")) {
            speechUnit = UnitSystem.fromValue(parseLine("Sp_Units", line).toIntOrNull() ?: -1)
        } else if (line.startsWith("Sp_Dec")) {
            speechDecimal = parseLine("Sp_Dec", line).toIntOrNull()
            if (speechMode != null && speechDecimal != null && speechDecimal != null) {
                return configFile.copy(
                    speeches = configFile.speeches + Speech(
                        mode = speechMode!!,
                        unit = speechUnit!!,
                        value = speechDecimal!!
                    )
                )
            }
        }
        return configFile
    }

    override fun maxLoop(): Int {
        return 11
    }

    override fun doReset() {
        speechMode = null
        speechUnit = null
        speechDecimal = null
    }
}