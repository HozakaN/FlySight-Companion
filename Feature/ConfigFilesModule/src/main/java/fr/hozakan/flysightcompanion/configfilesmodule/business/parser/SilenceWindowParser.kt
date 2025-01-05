package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.SilenceWindow
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode

class SilenceWindowParser : MultilineConfigItemParser() {

    private var windowTop: Int? = null
    private var windowBottom: Int? = null

    override fun configItemFilled(): Boolean {
        return windowTop != null && windowBottom != null
    }

    override fun key(): String {
        return "Win_Top"
    }

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        if (line.startsWith("Win_Top")) {
            windowTop = parseLine(key(), line).toIntOrNull() ?: -1
        } else if (line.startsWith("Win_Bottom")) {
            windowBottom = parseLine("Win_Bottom", line).toIntOrNull()
            if (windowTop != null && windowBottom != null) {
                return configFile.copy(
                    silenceWindows = configFile.silenceWindows + SilenceWindow(
                        top = windowTop!!,
                        bottom = windowBottom!!
                    )
                )
            }
        }
        return configFile
    }

    override fun doReset() {
        windowTop = null
        windowBottom = null
    }
}