package fr.hozakan.flysightcompanion.configfilesmodule.business

import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.DESCRIPTION_INDICATOR
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.CONFIG_NAME_INDICATOR
import fr.hozakan.flysightcompanion.configfilesmodule.business.parser.KIND_INDICATOR
import fr.hozakan.flysightcompanion.model.ConfigFile

class DefaultConfigEncoder : ConfigEncoder {
    override fun encodeConfig(configFile: ConfigFile): String {
        return buildString {
            appendLine("""
                ; For information on configuring FlySight, please go to
                ;     http://flysight.ca/wiki

                ; This file was generated with FlySight Multiplatform
                
                $CONFIG_NAME_INDICATOR:           ${configFile.name}
                $DESCRIPTION_INDICATOR:           ${configFile.description}
                $KIND_INDICATOR:                  ${configFile.kind}

                ; GPS settings

            """.trimIndent())
            appendLine("""
                Model:          ${configFile.dynamicModel.value} ; Dynamic model
                                  ;   0 = Portable
                                  ;   2 = Stationary
                                  ;   3 = Pedestrian
                                  ;   4 = Automotive
                                  ;   5 = Sea
                                  ;   6 = Airborne with < 1 G acceleration
                                  ;   7 = Airborne with < 2 G acceleration
                                  ;   8 = Airborne with < 4 G acceleration
            """.trimIndent())
            appendLine("Rate:         ${configFile.samplePeriod} ; Measurement rate (ms)")
            appendLine("""
              
                ; Tone settings

            """.trimIndent())
            appendLine("""
                Mode:           ${configFile.toneMode.value} ; Measurement mode
                                  ;   0 = Horizontal speed
                                  ;   1 = Vertical speed
                                  ;   2 = Glide ratio
                                  ;   3 = Inverse glide ratio
                                  ;   4 = Total speed
                                  ;   11 = Dive angle
            """.trimIndent())
            appendLine("""
                Min:            ${configFile.toneMinimum} ; Lowest pitch value
                                  ;   cm/s        in Mode 0, 1, or 4
                                  ;   ratio * 100 in Mode 2 or 3
                                  ;   degrees     in Mode 11
            """.trimIndent())
            appendLine("""
                Max:          ${configFile.toneMaximum} ; Highest pitch value
                                  ;   cm/s        in Mode 0, 1, or 4
                                  ;   ratio * 100 in Mode 2 or 3
                                  ;   degrees     in Mode 11
            """.trimIndent())
            appendLine("""
                Limits:         ${configFile.toneLimitBehaviour.value} ; Behaviour when outside bounds
                                  ;   0 = No tone
                                  ;   1 = Min/max tone
                                  ;   2 = Chirp up/down
                                  ;   3 = Chirp down/up
            """.trimIndent())
            appendLine("Volume:         ${configFile.toneVolume.value} ; 0 (min) to 8 (max)")
            appendLine("""
                
                ; Rate settings

            """.trimIndent())
            appendLine("""
                Mode_2:         ${configFile.rateMode.value} ; Determines tone rate
                                  ;   0 = Horizontal speed
                                  ;   1 = Vertical speed
                                  ;   2 = Glide ratio
                                  ;   3 = Inverse glide ratio
                                  ;   4 = Total speed
                                  ;   8 = Magnitude of Value 1
                                  ;   9 = Change in Value 1
                                  ;   11 = Dive angle
            """.trimIndent())
            appendLine("""
                Min_Val_2:    ${configFile.rateMinimumValue} ; Lowest rate value
                                  ;   cm/s          when Mode 2 = 0, 1, or 4
                                  ;   ratio * 100   when Mode 2 = 2 or 3
                                  ;   percent * 100 when Mode 2 = 9
                                  ;   degrees       when Mode 2 = 11
            """.trimIndent())
            appendLine("""
                Max_Val_2:   ${configFile.rateMaximumValue} ; Highest rate value
                                  ;   cm/s          when Mode 2 = 0, 1, or 4
                                  ;   ratio * 100   when Mode 2 = 2 or 3
                                  ;   percent * 100 when Mode 2 = 9
                                  ;   degrees       when Mode 2 = 11
            """.trimIndent())
            appendLine("""
                Min_Rate:     ${configFile.rateMinimum} ; Minimum rate (Hz * 100)
                Max_Rate:     ${configFile.rateMaximum} ; Maximum rate (Hz * 100)
            """.trimIndent())
            appendLine("""
                Flatline:       ${if (configFile.flatLineAtMinimumRate) 1 else 0} ; Flatline at minimum rate
                                  ;   0 = No
                                  ;   1 = Yes
            """.trimIndent())
            appendLine("""
                
                ; Speech settings

            """.trimIndent())
            appendLine("""
                Sp_Rate:        ${configFile.speechRate} ; Speech rate (s)
                                  ;   0 = No speech
            """.trimIndent())
            appendLine("""
                Sp_Volume:      ${configFile.speechVolume.value} ; 0 (min) to 8 (max)

            """.trimIndent())
            configFile.speeches.forEachIndexed { index, speech ->
                if (index == 0) {
                    appendLine("""
                        Sp_Mode:        ${speech.mode.value} ; Speech mode
                                          ;   0 = Horizontal speed
                                          ;   1 = Vertical speed
                                          ;   2 = Glide ratio
                                          ;   3 = Inverse glide ratio
                                          ;   4 = Total speed
                                          ;   5 = Altitude above DZ_Elev
                                          ;   11 = Dive angle
                        Sp_Units:       ${speech.unit.value} ; Speech units
                                          ;   0 = km/h
                                          ;   1 = mph
                        Sp_Dec:         ${speech.value} ; Speech precision
                                          ;   Altitude step in Mode 5
                                          ;   Decimal places in all other Modes
                    """.trimIndent())
                } else {
                    appendLine("""
                        
                        Sp_Mode:        ${speech.mode.value} ; Speech mode
                        Sp_Units:       ${speech.unit.value} ; Speech units
                        Sp_Dec:         ${speech.value} ; Speech precision
                    """.trimIndent())
                }
            }
            appendLine("""
                
                ; Thresholds

            """.trimIndent())
            appendLine("""
                V_Thresh:    ${configFile.verticalThreshold/*.div(0.036)*/} ; Minimum vertical speed for tone (cm/s)
                H_Thresh:    ${configFile.horizontalThreshold/*.div(0.036)*/} ; Minimum horizontal speed for tone (cm/s)
            """.trimIndent())
            appendLine("""
                
                ; Miscellaneous

            """.trimIndent())
            appendLine("""
                Use_SAS:        ${if (configFile.useSAS) 1 else 0} ; Use skydiver's airspeed
                                  ;   0 = No
                                  ;   1 = Yes
            """.trimIndent())
            appendLine("""
                TZ_Offset:      0 ; Timezone offset of output files in seconds
                                  ;   -14400 = UTC-4 (EDT)
                                  ;   -18000 = UTC-5 (EST, CDT)
                                  ;   -21600 = UTC-6 (CST, MDT)
                                  ;   -25200 = UTC-7 (MST, PDT)
                                  ;   -28800 = UTC-8 (PST)
            """.trimIndent())
            appendLine("""
                
                ; Initialization

            """.trimIndent())
            appendLine("""
                Init_Mode:      ${configFile.initMode.value} ; When the FlySight is powered on
                                  ;   0 = Do nothing
                                  ;   1 = Test speech mode
                                  ;   2 = Play file
            """.trimIndent())
            appendLine("Init_File:      ${configFile.initFile ?: "0"} ; File to be played")
            appendLine("""
                
                ; Alarm settings

                ; WARNING: GPS measurements depend on very weak signals
                ;          received from orbiting satellites. As such, they
                ;          are prone to interference, and should NEVER be
                ;          relied upon for life saving purposes.

                ;          UNDER NO CIRCUMSTANCES SHOULD THESE ALARMS BE
                ;          USED TO INDICATE DEPLOYMENT OR BREAKOFF ALTITUDE.

                ; NOTE:    Alarm elevations are given in meters above ground
                ;          elevation, which is specified in DZ_Elev.
                
            """.trimIndent())
            appendLine("""
                Window:         0 ; Alarm window (m)
                Win_Above:      ${configFile.windowAbove} ; Alarm window (m)
                Win_Below:      ${configFile.windowBelow} ; Alarm window (m)
                DZ_Elev:        ${configFile.dzElev} ; Ground elevation (m above sea level)
                
            """.trimIndent())
            configFile.alarms.forEachIndexed { index, alarm ->
                if (index == 0) {
                    appendLine("""
                        Alarm_Elev:     ${alarm.alarmElevation} ; Alarm elevation (m above ground level)
                        Alarm_Type:     ${alarm.alarmType.value} ; Alarm type
                                          ;   0 = No alarm
                                          ;   1 = Beep
                                          ;   2 = Chirp up
                                          ;   3 = Chirp down
                                          ;   4 = Play file
                        Alarm_File:     ${alarm.alarmFile} ; File to be played
                    """.trimIndent())
                } else {
                    appendLine("""
                        
                        Alarm_Elev:     ${alarm.alarmElevation} ; Alarm elevation (m above ground level)
                        Alarm_Type:     ${alarm.alarmType.value} ; Alarm type
                        Alarm_File:     ${alarm.alarmFile} ; File to be played
                    """.trimIndent())
                }
            }
            appendLine("""
                
                ; Altitude mode settings

                ; WARNING: GPS measurements depend on very weak signals
                ;          received from orbiting satellites. As such, they
                ;          are prone to interference, and should NEVER be
                ;          relied upon for life saving purposes.

                ;          UNDER NO CIRCUMSTANCES SHOULD ALTITUDE MODE BE
                ;          USED TO INDICATE DEPLOYMENT OR BREAKOFF ALTITUDE.

                ; NOTE:    Altitude is given relative to ground elevation,
                ;          which is specified in DZ_Elev. Altitude mode will
                ;          not function below 1500 m above ground.

            """.trimIndent())
            appendLine("""
                Alt_Units:      ${configFile.altitudeUnit.value} ; Altitude units
                                  ;   0 = m
                                  ;   1 = ft
                Alt_Step:       ${configFile.altitudeStep} ; Altitude between announcements
                                  ;   0 = No altitude

            """.trimIndent())
            appendLine("""
                
                ; NOTE:    Silence windows are given in meters above ground
                ;          elevation, which is specified in DZ_Elev. Tones
                ;          will be silenced during these windows and only
                ;          alarms will be audible.

            """.trimIndent())
            configFile.silenceWindows.forEach { silenceWindow ->
                appendLine("""
                    Win_Top:        ${silenceWindow.top} ; Silence window top (m)
                    Win_Bottom:     ${silenceWindow.bottom} ; Silence window bottom (m)

                """.trimIndent())
            }
        }
    }
}

private val encoders = listOf(
    ""
)