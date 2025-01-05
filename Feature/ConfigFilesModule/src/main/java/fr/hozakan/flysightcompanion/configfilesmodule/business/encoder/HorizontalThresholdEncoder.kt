package fr.hozakan.flysightcompanion.configfilesmodule.business.encoder

import fr.hozakan.flysightcompanion.model.ConfigFile

class HorizontalThresholdEncoder : ConfigItemEncoder() {
    override fun encode(sb: StringBuilder, configFile: ConfigFile) {
        sb.appendLine("V_Thresh: ${configFile.horizontalThreshold.div(0.036)} ; Minimum horizontal speed for tone (cm/s)")
    }
}