package fr.hozakan.flysightcompanion.configfilesmodule.business.encoder

import fr.hozakan.flysightcompanion.model.ConfigFile

class VerticalThresholdEncoder : ConfigItemEncoder() {
    override fun encode(sb: StringBuilder, configFile: ConfigFile) {
        sb.appendLine("V_Thresh: ${configFile.verticalThreshold.div(0.036)} ; Minimum vertical speed for tone (cm/s)")
    }
}