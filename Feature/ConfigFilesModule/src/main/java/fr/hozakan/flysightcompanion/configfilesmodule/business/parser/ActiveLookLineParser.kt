package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.ActiveLookLine
import fr.hozakan.flysightcompanion.model.config.ActiveLookLineType
import fr.hozakan.flysightcompanion.model.config.UnitSystem

class ActiveLookLineParser : MultilineConfigItemParser() {

    private var lineType: ActiveLookLineType? = null
    private var unitSystem: UnitSystem? = null
    private var decimal: Int? = null

    override fun configItemFilled(): Boolean {
        return lineType != null && unitSystem != null && decimal != null
    }

    override fun key(): String {
        return "AL_Line"
    }

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        if (line.startsWith("AL_Line")) {
            lineType = ActiveLookLineType.fromValue(parseLine("AL_Line", line).toIntOrNull() ?: -1)
        } else if (line.startsWith("AL_Units")) {
            unitSystem = UnitSystem.fromValue(parseLine("AL_Units", line).toIntOrNull() ?: -1)
        } else if (line.startsWith("AL_Dec")) {
            decimal = parseLine("AL_Dec", line).toIntOrNull()
            if (lineType != null && unitSystem != null && decimal != null) {
                return configFile.copy(
                    activeLook = configFile.activeLook.copy(
                        lines = configFile.activeLook.lines + ActiveLookLine(
                            type = lineType!!,
                            unitSystem = unitSystem!!,
                            decimal = decimal!!
                        )
                    )
                )
            }
        }
        return configFile
    }

    override fun maxLoop(): Int {
        return 20
    }

    override fun doReset() {
        lineType = null
        unitSystem = null
        decimal = null
    }
}