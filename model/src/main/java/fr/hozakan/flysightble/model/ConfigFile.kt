package fr.hozakan.flysightble.model

import fr.hozakan.flysightble.model.config.Alarm
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.InitMode
import fr.hozakan.flysightble.model.config.RateMode
import fr.hozakan.flysightble.model.config.SilenceWindow
import fr.hozakan.flysightble.model.config.Speech
import fr.hozakan.flysightble.model.config.ToneLimitBehaviour
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.config.UnitSystem
import fr.hozakan.flysightble.model.config.Volume

/*
TODO Pay attention to unit system and Rate value (does it change when changing unit system?)
 */
data class ConfigFile(
    val name: String,
    val description: String,
    val kind: String,
    //General
    val dynamicModel: DynamicModel,
    val samplePeriod: Int,
    //Tone
    val toneMode: ToneMode,
    val toneMinimum: Int,
    val toneMaximum: Int,
    val toneLimitBehaviour: ToneLimitBehaviour,
    val toneVolume: Volume,
    //Rate
    val rateMode: RateMode,
    val rateMinimumValue: Int,
    val rateMaximumValue: Int,
    val rateMinimum: Int,
    val rateMaximum: Int,
    val flatLineAtMinimumRate: Boolean,
    //Speech
    val speechRate: Int,
    val speechVolume: Volume,
    val speeches: List<Speech>,
    //Thresholds
    val verticalThreshold: Int, // (cm/s)
    val horizontalThreshold: Int, // (cm/s)
    //Miscellaneous
    val tzOffset: Int,
    val useSAS: Boolean,
    //initialization
    val initMode: InitMode,
    val initFile: String?,
    //Alarm settings
    val windowAbove: Int,
    val windowBelow: Int,
    val dzElev: Int,
    val alarms: List<Alarm>,
    //Altitude
    val altitudeStep: Int, //Altitude between announcements
    val altitudeUnit: UnitSystem,
    //silence windows
    val silenceWindows: List<SilenceWindow>
)

private val defaultConfigFile = ConfigFile(
    name = "",
    description = "",
    kind = "",
    dynamicModel = DynamicModel.Airborne2g,
    samplePeriod = 200,
    toneMode = ToneMode.GlideRatio,
    toneMinimum = 0,
    toneMaximum = 300,
    toneLimitBehaviour = ToneLimitBehaviour.MinMaxTone,
    toneVolume = Volume.Volume0,
    rateMode = RateMode.ChangeInValue1,
    rateMinimumValue = 300,
    rateMaximumValue = 1500,
    rateMinimum = 100,
    rateMaximum = 500,
    flatLineAtMinimumRate = false,
    speechRate = 0,
    speechVolume = Volume.Volume0,
    speeches = emptyList(),
    verticalThreshold = 1000,
    horizontalThreshold = 0,
    tzOffset = 0,
    useSAS = true,
    initMode = InitMode.DoNothing,
    initFile = "0",
    windowAbove = 0,
    windowBelow = 0,
    dzElev = 0,
    alarms = emptyList(),
    altitudeUnit = UnitSystem.Metric,
    altitudeStep = 0,
    silenceWindows = emptyList()
)

fun defaultConfigFile() = defaultConfigFile.copy()