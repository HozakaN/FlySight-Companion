package fr.hozakan.flysightcompanion.configfilesmodule.business.parser

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.Alarm
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode

class AlarmParser : MultilineConfigItemParser() {

    private var alarmElev: Int? = null
    private var alarmType: AlarmType? = null
    private var alarmFile: String? = null

    override fun configItemFilled(): Boolean {
        return alarmElev != null && alarmType != null && alarmFile != null
    }

    override fun key(): String {
        return "Alarm_Elev"
    }

    override fun fillConfigFile(line: String, configFile: ConfigFile): ConfigFile {
        if (line.startsWith("Alarm_Elev")) {
            alarmElev = parseLine("Alarm_Elev", line).toIntOrNull() ?: -1
        } else if (line.startsWith("Alarm_Type")) {
            alarmType = AlarmType.fromValue(parseLine("Alarm_Type", line).toIntOrNull() ?: -1)
        } else if (line.startsWith("Alarm_File")) {
            alarmFile = parseLine("Alarm_File", line)
            if (alarmElev != null && alarmType != null) {
                return configFile.copy(
                    alarms = configFile.alarms + Alarm(
                        alarmElevation = alarmElev!!,
                        alarmType = alarmType!!,
                        alarmFile = alarmFile!!
                    )
                )
            }
        }
        return configFile
    }

    override fun doReset() {
        alarmElev = null
        alarmType = null
        alarmFile = null
    }
}