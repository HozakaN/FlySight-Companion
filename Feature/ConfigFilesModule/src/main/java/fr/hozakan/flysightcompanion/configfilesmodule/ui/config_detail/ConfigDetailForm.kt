package fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.ActiveLook
import fr.hozakan.flysightcompanion.model.config.ActiveLookLine
import fr.hozakan.flysightcompanion.model.config.ActiveLookLineType
import fr.hozakan.flysightcompanion.model.config.ActiveLookMode
import fr.hozakan.flysightcompanion.model.config.Alarm
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.Navigation
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.SilenceWindow
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.config.Volume
import fr.hozakan.flysightcompanion.model.defaultConfigFile
import fr.hozakan.flysightcompanion.model.emptyConfigFile

@Composable
fun rememberConfigDetailForm(
    initialConfigFile: ConfigFile = emptyConfigFile()
): ConfigDetailForm {
    return remember(initialConfigFile) {
        ConfigDetailForm(initialConfigFile)
    }
}

@Stable
class ConfigDetailForm(
    initialForm: ConfigFile = emptyConfigFile()
) {

    internal var isDirty by mutableStateOf(false)
    internal var hasValidFileName by mutableStateOf(true)
    internal var isValid by mutableStateOf(true)

    internal var name by mutableStateOf<String?>(initialForm.name)
    internal var description by mutableStateOf<String?>(initialForm.description)
    internal var group by mutableStateOf<String?>(initialForm.group)

    internal var dynamicModel by mutableStateOf(initialForm.dynamicModel)

    internal var samplePeriod by mutableStateOf<Int?>(initialForm.samplePeriod)

    //Tone
    internal var toneMode by mutableStateOf(initialForm.toneMode)
    internal var toneMinimum by mutableStateOf<Int?>(initialForm.toneMinimum)
    internal var toneMaximum by mutableStateOf<Int?>(initialForm.toneMaximum)
    internal var toneLimitBehaviour by mutableStateOf(initialForm.toneLimitBehaviour)
    internal var toneVolume by mutableStateOf(initialForm.toneVolume)

    //Rate
    internal var rateMode by mutableStateOf(initialForm.rateMode)
    internal var rateMinimumValue by mutableStateOf<Int?>(initialForm.rateMinimumValue)
    internal var rateMaximumValue by mutableStateOf<Int?>(initialForm.rateMaximumValue)
    internal var rateMinimum by mutableStateOf<Int?>(initialForm.rateMinimum)
    internal var rateMaximum by mutableStateOf<Int?>(initialForm.rateMaximum)
    internal var flatLineAtMinimumRate by mutableStateOf(initialForm.flatLineAtMinimumRate)

    //Speech
    internal var speechRate by mutableStateOf<Int?>(initialForm.speechRate)
    internal var speechVolume by mutableStateOf(initialForm.speechVolume)
    internal var speeches by mutableStateOf(initialForm.speeches)

    //Thresholds
    internal var verticalThreshold by mutableStateOf<Int?>(initialForm.verticalThreshold)
    internal var horizontalThreshold by mutableStateOf<Int?>(initialForm.horizontalThreshold)

    //Miscellaneous
    internal var tzOffset by mutableStateOf<Int?>(initialForm.tzOffset)
    internal var useSAS by mutableStateOf(initialForm.useSAS)

    //initialization
    internal var initMode by mutableStateOf(initialForm.initMode)
    internal var initFile by mutableStateOf(initialForm.initFile)

    //Alarm settings
    internal var windowAbove by mutableStateOf<Int?>(initialForm.windowAbove)
    internal var windowBelow by mutableStateOf<Int?>(initialForm.windowBelow)
    internal var dzElev by mutableStateOf<Int?>(initialForm.dzElev)
    internal var alarms by mutableStateOf(initialForm.alarms)

    //Altitude
    internal var altitudeStep by mutableStateOf<Int?>(initialForm.altitudeStep)
    internal var altitudeUnit by mutableStateOf(initialForm.altitudeUnit)

    //silence windows
    internal var silenceWindows by mutableStateOf(initialForm.silenceWindows)
    
    //Navigation
    internal var navigationDeviceId by mutableStateOf<String?>(initialForm.navigation.deviceId)
    internal var navigationLat by mutableStateOf<Int?>(initialForm.navigation.lat)
    internal var navigationLon by mutableStateOf<Int?>(initialForm.navigation.lon)
    internal var navigationBearing by mutableStateOf<Int?>(initialForm.navigation.bearing)
    internal var navigationEndNav by mutableStateOf<Int?>(initialForm.navigation.endNav)
    internal var navigationMaxDist by mutableStateOf<Int?>(initialForm.navigation.maxDist)
    internal var navigationMinAngle by mutableStateOf<Int?>(initialForm.navigation.minAngle)

    //ActiveLook
    internal var activeLookDeviceId by mutableStateOf<String?>(initialForm.activeLook.deviceId)
    internal var activeLookMode by mutableStateOf(initialForm.activeLook.mode)
    internal var activeLookRate by mutableStateOf<Int?>(initialForm.activeLook.rate)
    internal var activeLookLines by mutableStateOf(initialForm.activeLook.lines)

    fun updateConfigFileName(fileName: String) {
        name = fileName
        isDirty = true
        checkValidity()
        hasValidFileName = fileName.isNotBlank()
    }

    fun updateConfigFileDescription(description: String) {
        this.description = description
        isDirty = true
        checkValidity()
    }

    fun updateConfigFileGroup(group: String) {
        this.group = group
        isDirty = true
        checkValidity()
    }

    fun updateDynamicModel(dynamicModel: DynamicModel) {
        this.dynamicModel = dynamicModel
        isDirty = true
        checkValidity()
    }

    fun updateSamplePeriodToDefaultValue() {
        updateSamplePeriod(defaultConfigFile.samplePeriod)
    }

    fun updateSamplePeriod(samplePeriod: Int?) {
        this.samplePeriod = samplePeriod
        isDirty = true
        checkValidity()
    }

    fun updateToneMode(toneMode: ToneMode) {
        this.toneMode = toneMode
        isDirty = true
        checkValidity()
    }

    fun updateToneMinimumToDefaultValue() {
        updateToneMinimum(defaultConfigFile.toneMinimum)
    }

    fun updateToneMinimum(toneMinimum: Int?) {
        this.toneMinimum = toneMinimum
        isDirty = true
        checkValidity()
    }

    fun updateToneMaximumToDefaultValue() {
        updateToneMaximum(defaultConfigFile.toneMaximum)
    }

    fun updateToneMaximum(toneMaximum: Int?) {
        this.toneMaximum = toneMaximum
        isDirty = true
        checkValidity()
    }

    fun updateToneLimitBehaviour(toneLimitBehaviour: ToneLimitBehaviour) {
        this.toneLimitBehaviour = toneLimitBehaviour
        isDirty = true
        checkValidity()
    }

    fun updateToneVolume(toneVolume: Volume) {
        this.toneVolume = toneVolume
        isDirty = true
        checkValidity()
    }

    fun updateRateMode(rateMode: RateMode) {
        this.rateMode = rateMode
        isDirty = true
        checkValidity()
    }

    fun updateRateMinimumValueToDefaultValue() {
        updateRateMinimumValue(defaultConfigFile.rateMinimumValue)
    }

    fun updateRateMinimumValue(rateMinimumValue: Int?) {
        this.rateMinimumValue = rateMinimumValue
        isDirty = true
        checkValidity()
    }

    fun updateRateMaximumValueToDefaultValue() {
        updateRateMaximumValue(defaultConfigFile.rateMaximumValue)
    }

    fun updateRateMaximumValue(rateMaximumValue: Int?) {
        this.rateMaximumValue = rateMaximumValue
        isDirty = true
        checkValidity()
    }

    fun updateRateMinimumToDefaultValue() {
        updateRateMinimum(defaultConfigFile.rateMinimum)
    }

    fun updateRateMinimum(rateMinimum: Int?) {
        this.rateMinimum = rateMinimum
        isDirty = true
        checkValidity()
    }

    fun updateRateMaximumToDefaultValue() {
        updateRateMaximum(defaultConfigFile.rateMaximum)
    }

    fun updateRateMaximum(rateMaximum: Int?) {
        this.rateMaximum = rateMaximum
        isDirty = true
        checkValidity()
    }

    fun updateFlatLineAtMinimumRate(flatLineAtMinimumRate: Boolean) {
        this.flatLineAtMinimumRate = flatLineAtMinimumRate
        isDirty = true
        checkValidity()
    }

    fun updateSpeechRateToDefaultValue() {
        updateSpeechRate(defaultConfigFile.speechRate)
    }

    fun updateSpeechRate(speechRate: Int?) {
        this.speechRate = speechRate
        isDirty = true
        checkValidity()
    }

    fun updateSpeechVolume(speechVolume: Volume) {
        this.speechVolume = speechVolume
        isDirty = true
        checkValidity()
    }

    fun addSpeech(speech: Speech) {
        this.speeches += speech
        isDirty = true
    }

    fun deleteSpeech(speech: Speech) {
        this.speeches -= speech
        isDirty = true
    }

    fun updateVerticalThresholdToDefaultValue() {
        updateVerticalThreshold(defaultConfigFile.verticalThreshold)
    }

    fun updateVerticalThreshold(verticalThreshold: Int?) {
        this.verticalThreshold = verticalThreshold
        isDirty = true
        checkValidity()
    }

    fun updateHorizontalThresholdToDefaultValue() {
        updateHorizontalThreshold(defaultConfigFile.horizontalThreshold)
    }

    fun updateHorizontalThreshold(horizontalThreshold: Int?) {
        this.horizontalThreshold = horizontalThreshold
        isDirty = true
        checkValidity()
    }

    fun updateUseSAS(useSAS: Boolean) {
        this.useSAS = useSAS
        isDirty = true
        checkValidity()
    }

    fun updateInitMode(initMode: InitMode) {
        this.initMode = initMode
        isDirty = true
        checkValidity()
    }

    fun updateInitFile(initFile: String?) {
        this.initFile = initFile
        isDirty = true
        checkValidity()
    }

    fun updateAltitudeStep(altitudeStep: Int?) {
        this.altitudeStep = altitudeStep
        isDirty = true
        checkValidity()
    }

    fun updateWindowAbove(windowAbove: Int?) {
        this.windowAbove = windowAbove
        isDirty = true
        checkValidity()
    }

    fun updateWindowBelow(windowBelow: Int?) {
        this.windowBelow = windowBelow
        isDirty = true
        checkValidity()
    }

    fun updateDzElev(dzElev: Int?) {
        this.dzElev = dzElev
        isDirty = true
        checkValidity()
    }

    fun addAlarm(alarm: Alarm) {
        this.alarms += alarm
        isDirty = true
    }

    fun deleteAlarm(alarm: Alarm) {
        this.alarms -= alarm
        isDirty = true
    }

    fun updateAltitudeUnit(altitudeUnit: UnitSystem) {
        this.altitudeUnit = altitudeUnit
        isDirty = true
        checkValidity()
    }

    fun addSilenceWindow(silenceWindow: SilenceWindow) {
        this.silenceWindows += silenceWindow
        isDirty = true
    }

    fun deleteSilenceWindow(silenceWindow: SilenceWindow) {
        this.silenceWindows -= silenceWindow
        isDirty = true
    }

    fun updateNavigationDeviceId(deviceId: String?) {
        this.navigationDeviceId = deviceId
        isDirty = true
        checkValidity()
    }

    fun updateNavigationLat(lat: Int?) {
        this.navigationLat = lat
        isDirty = true
        checkValidity()
    }

    fun updateNavigationLon(lon: Int?) {
        this.navigationLon = lon
        isDirty = true
        checkValidity()
    }

    fun updateNavigationBearing(bearing: Int?) {
        this.navigationBearing = bearing
        isDirty = true
        checkValidity()
    }

    fun updateNavigationEndNav(endNav: Int?) {
        this.navigationEndNav = endNav
        isDirty = true
        checkValidity()
    }

    fun updateNavigationMaxDist(maxDist: Int?) {
        this.navigationMaxDist = maxDist
        isDirty = true
        checkValidity()
    }

    fun updateNavigationMinAngle(minAngle: Int?) {
        this.navigationMinAngle = minAngle
        isDirty = true
        checkValidity()
    }

    fun updateActiveLookDeviceId(deviceId: String?) {
        this.activeLookDeviceId = deviceId
        isDirty = true
        checkValidity()
    }

    fun updateActiveLookMode(mode: ActiveLookMode) {
        this.activeLookMode = mode
        isDirty = true
        checkValidity()
    }

    fun updateActiveLookRate(rate: Int?) {
        this.activeLookRate = rate
        isDirty = true
        checkValidity()
    }

    fun addActiveLookLine(line: ActiveLookLine) {
        this.activeLookLines += line
        isDirty = true
    }

    fun deleteActiveLookLine(line: ActiveLookLine) {
        this.activeLookLines -= line
        isDirty = true
    }

    private fun checkValidity() {
        isValid =
            name != null &&
                    description != null &&
                    group != null &&
                    samplePeriod != null &&
                    toneMinimum != null &&
                    toneMaximum != null &&
                    rateMinimumValue != null &&
                    rateMaximumValue != null &&
                    rateMinimum != null &&
                    rateMaximum != null &&
                    speechRate != null &&
                    verticalThreshold != null &&
                    horizontalThreshold != null &&
                    tzOffset != null &&
                    windowAbove != null &&
                    windowBelow != null &&
                    dzElev != null &&
                    altitudeStep != null
    }

    fun toConfigFile(): ConfigFile? {
        return ConfigFile(
            name = name ?: return null,
            description = description ?: return null,
            group = group ?: return null,
            dynamicModel = dynamicModel,
            samplePeriod = samplePeriod ?: return null,
            toneMode = toneMode,
            toneMinimum = toneMinimum ?: return null,
            toneMaximum = toneMaximum ?: return null,
            toneLimitBehaviour = toneLimitBehaviour,
            toneVolume = toneVolume,
            rateMode = rateMode,
            rateMinimumValue = rateMinimumValue ?: return null,
            rateMaximumValue = rateMaximumValue ?: return null,
            rateMinimum = rateMinimum ?: return null,
            rateMaximum = rateMaximum ?: return null,
            flatLineAtMinimumRate = flatLineAtMinimumRate,
            speechRate = speechRate ?: return null,
            speechVolume = speechVolume,
            speeches = speeches,
            verticalThreshold = verticalThreshold ?: return null,
            horizontalThreshold = horizontalThreshold ?: return null,
            tzOffset = tzOffset ?: return null,
            useSAS = useSAS,
            initMode = initMode,
            initFile = initFile,
            windowAbove = windowAbove ?: return null,
            windowBelow = windowBelow ?: return null,
            dzElev = dzElev ?: return null,
            alarms = alarms,
            altitudeStep = altitudeStep ?: return null,
            altitudeUnit = altitudeUnit,
            silenceWindows = silenceWindows,
            navigation = Navigation(
                deviceId = navigationDeviceId ?: "",
                lat = navigationLat ?: 0,
                lon = navigationLon ?: 0,
                bearing = navigationBearing ?: 0,
                endNav = navigationEndNav ?: 1500,
                maxDist = navigationMaxDist ?: 10000,
                minAngle = navigationMinAngle ?: 5
            ),
            activeLook = ActiveLook(
                deviceId = activeLookDeviceId ?: "000000",
                mode = activeLookMode,
                rate = activeLookRate ?: 1000,
                lines = activeLookLines
            )
        )
    }
}