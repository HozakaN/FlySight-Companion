package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.ActiveLook
import fr.hozakan.flysightcompanion.model.config.ActiveLookLine
import fr.hozakan.flysightcompanion.model.config.ActiveLookLineType
import fr.hozakan.flysightcompanion.model.config.ActiveLookMode
import fr.hozakan.flysightcompanion.model.config.Alarm
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.Navigation
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.SilenceWindow
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.config.Volume
import fr.hozakan.flysightcompanion.model.defaultConfigFile
import fr.hozakan.flysightcompanion.model.session.profile.DisplayGrid
import fr.hozakan.flysightcompanion.model.session.profile.DisplayItem
import fr.hozakan.flysightcompanion.model.session.profile.DisplayItemBundle
import fr.hozakan.flysightcompanion.model.session.profile.DisplayableCapability
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.model.session.profile.Coordinate
import timber.log.Timber

@Composable
fun rememberSessionProfileForm(
    initialConfiguration: SessionProfile = SessionProfile.default()
): SessionProfileForm {
    return rememberSaveable(
        initialConfiguration,
        saver = SessionProfileForm.Saver
    ) {
        SessionProfileForm(initialConfiguration)
    }
}

@Stable
class SessionProfileForm(
    initialConfiguration: SessionProfile = SessionProfile.default()
) {
    companion object {
        val Saver: Saver<SessionProfileForm, Any> = listSaver(
            save = { form ->
                val savedList = mutableListOf<Any>(
                    form.isDirty,
                    form.hasValidProfileName,
                    form.isValid,
                    form.name ?: "",
                    form.description ?: ""
                )
                
                // Save all ConfigFile fields
                val configFile = form.configFile
                if (configFile != null) {
                    savedList.add(true) // ConfigFile exists flag
                    savedList.add(configFile.name)
                    savedList.add(configFile.description)
                    savedList.add(configFile.group)
                    savedList.add(configFile.dynamicModel.name)
                    savedList.add(configFile.samplePeriod)
                    savedList.add(configFile.toneMode.name)
                    savedList.add(configFile.toneMinimum)
                    savedList.add(configFile.toneMaximum)
                    savedList.add(configFile.toneLimitBehaviour.name)
                    savedList.add(configFile.toneVolume.name)
                    savedList.add(configFile.rateMode.name)
                    savedList.add(configFile.rateMinimumValue)
                    savedList.add(configFile.rateMaximumValue)
                    savedList.add(configFile.rateMinimum)
                    savedList.add(configFile.rateMaximum)
                    savedList.add(configFile.flatLineAtMinimumRate)
                    savedList.add(configFile.speechRate)
                    savedList.add(configFile.speechVolume.name)
                    
                    // Save speeches with all fields
                    savedList.add(configFile.speeches.size)
                    configFile.speeches.forEach { speech ->
                        savedList.add(speech.mode.name)
                        savedList.add(speech.unit.name)
                        savedList.add(speech.value)
                    }
                    
                    savedList.add(configFile.verticalThreshold)
                    savedList.add(configFile.horizontalThreshold)
                    savedList.add(configFile.tzOffset)
                    savedList.add(configFile.useSAS)
                    savedList.add(configFile.initMode.name)
                    savedList.add(configFile.initFile ?: "")
                    savedList.add(configFile.windowAbove)
                    savedList.add(configFile.windowBelow)
                    savedList.add(configFile.dzElev)
                    
                    // Save alarms with all fields
                    savedList.add(configFile.alarms.size)
                    configFile.alarms.forEach { alarm ->
                        savedList.add(alarm.alarmType.name)
                        savedList.add(alarm.alarmElevation)
                        savedList.add(alarm.alarmFile)
                    }
                    
                    savedList.add(configFile.altitudeUnit.name)
                    savedList.add(configFile.altitudeStep)
                    
                    // Save silence windows with all fields
                    savedList.add(configFile.silenceWindows.size)
                    configFile.silenceWindows.forEach { silenceWindow ->
                        savedList.add(silenceWindow.top)
                        savedList.add(silenceWindow.bottom)
                    }
                    
                    // Save navigation
                    val navigation = configFile.navigation
                    if (navigation != null) {
                        savedList.add(true) // Navigation exists
                        savedList.add(navigation.deviceId)
                        savedList.add(navigation.lat)
                        savedList.add(navigation.lon)
                        savedList.add(navigation.bearing)
                        savedList.add(navigation.endNav)
                        savedList.add(navigation.maxDist)
                        savedList.add(navigation.minAngle)
                    } else {
                        savedList.add(false) // Navigation doesn't exist
                    }
                    
                    // Save ActiveLook with all fields
                    savedList.add(configFile.activeLook.deviceId)
                    savedList.add(configFile.activeLook.mode.name)
                    savedList.add(configFile.activeLook.rate)
                    savedList.add(configFile.activeLook.lines.size)
                    configFile.activeLook.lines.forEach { line ->
                        savedList.add(line.type.name)
                        savedList.add(line.unitSystem.name)
                        savedList.add(line.decimal)
                    }
                } else {
                    savedList.add(false) // ConfigFile doesn't exist
                }
                
                // Save showMap, displayPerformanceLane, displayPerformanceLaneInMap
                savedList.add(form.showMap)
                savedList.add(form.displayPerformanceLane)
                savedList.add(form.displayPerformanceLaneInMap)

                // Save the whole ReferencePoint
                val refPoint = form.referencePoint
                if (refPoint != null) {
                    savedList.add(true) // ReferencePoint exists
                    savedList.add(refPoint.id)
                    savedList.add(refPoint.name)
                    savedList.add(refPoint.description)
                    savedList.add(refPoint.coords.latitude)
                    savedList.add(refPoint.coords.longitude)
                } else {
                    savedList.add(false) // No ReferencePoint
                }

                savedList.add(form.displayGrid.name)
                savedList.add(form.showGridLines)
                savedList.add(form.displayItems.size)
                
                // Save each DisplayItem
                form.displayItems.forEach { displayItem ->
                    savedList.add(displayItem.displayableCapability.name)
                    savedList.add(displayItem.caseIndex)
                    savedList.add(displayItem.indexInCase)
                    
                    // Handle DisplayItemBundle
                    val bundle = displayItem.bag
                    if (bundle == null) {
                        savedList.add(false) // No bundle
                    } else {
                        savedList.add(true) // Has bundle
                        when (bundle) {
                            is DisplayItemBundle.DistanceToRefPointBundle -> {
                                savedList.add("DistanceToRefPointBundle")
                                // Save the entire ReferencePoint
                                val refPoint = bundle.referencePoint
                                savedList.add(refPoint.id)
                                savedList.add(refPoint.name)
                                savedList.add(refPoint.description)
                                savedList.add(refPoint.coords.latitude)
                                savedList.add(refPoint.coords.longitude)
                            }
                        }
                    }
                }
                
                savedList.addAll(listOf(
                    form.useUSForTTS,
                    form.performanceLaneWidth,
                    form.competitionWindowTop,
                    form.competitionWindowBottom,
                    form.exitDetectionWindowTop,
                    form.exitDetectionWindowBottom,
                    form.showVisualAlertWhenExitDetected,
                    form.playAudioAlertWhenExitDetected,
                    form.showVisualAlertWhenNotInWindowBeforeExit,
                    form.exitPointsDown,
                    form.exitPointsUp,
                    form.exitDownThresh,
                    form.exitUpThresh,
                    form.exitDetectionConfirmationDuration,
                    form.timeAfterExit,
                    form.displayFlareDetector
                ))
                
                savedList
            },
            restore = { savedList ->
                try {
                    // Create a form with default settings first
                    val form = SessionProfileForm()
                    
                    // Then restore the saved state
                    var index = 0
                    form.isDirty = savedList[index++] as Boolean
                    form.hasValidProfileName = savedList[index++] as Boolean
                    form.isValid = savedList[index++] as Boolean
                    form.name = savedList[index++] as String?
                    form.description = savedList[index++] as String?
                    
                    // Restore ConfigFile from all saved fields
                    val configFileExists = savedList[index++] as Boolean
                    if (configFileExists) {
                        val name = savedList[index++] as String
                        val description = savedList[index++] as String
                        val group = savedList[index++] as String
                        val dynamicModel = DynamicModel.valueOf(savedList[index++] as String)
                        val samplePeriod = savedList[index++] as Int
                        val toneMode = ToneMode.valueOf(savedList[index++] as String)
                        val toneMinimum = savedList[index++] as Int
                        val toneMaximum = savedList[index++] as Int
                        val toneLimitBehaviour = ToneLimitBehaviour.valueOf(savedList[index++] as String)
                        val toneVolume = Volume.valueOf(savedList[index++] as String)
                        val rateMode = RateMode.valueOf(savedList[index++] as String)
                        val rateMinimumValue = savedList[index++] as Int
                        val rateMaximumValue = savedList[index++] as Int
                        val rateMinimum = savedList[index++] as Int
                        val rateMaximum = savedList[index++] as Int
                        val flatLineAtMinimumRate = savedList[index++] as Boolean
                        val speechRate = savedList[index++] as Int
                        val speechVolume = Volume.valueOf(savedList[index++] as String)
                        
                        // Restore speeches
                        val speechesSize = savedList[index++] as Int
                        val speeches = mutableListOf<Speech>()
                        for (i in 0 until speechesSize) {
                            val speechMode = SpeechMode.valueOf(savedList[index++] as String)
                            val speechUnit = UnitSystem.valueOf(savedList[index++] as String)
                            val speechValue = savedList[index++] as Int
                            speeches.add(Speech(speechMode, speechUnit, speechValue))
                        }
                        
                        val verticalThreshold = savedList[index++] as Int
                        val horizontalThreshold = savedList[index++] as Int
                        val tzOffset = savedList[index++] as Int
                        val useSAS = savedList[index++] as Boolean
                        val initMode = InitMode.valueOf(savedList[index++] as String)
                        val initFile = (savedList[index++] as String).let { it.ifEmpty { null } }
                        val windowAbove = savedList[index++] as Int
                        val windowBelow = savedList[index++] as Int
                        val dzElev = savedList[index++] as Int
                        
                        // Restore alarms
                        val alarmsSize = savedList[index++] as Int
                        val alarms = mutableListOf<Alarm>()
                        for (i in 0 until alarmsSize) {
                            val alarmType = AlarmType.valueOf(savedList[index++] as String)
                            val alarmElevation = savedList[index++] as Int
                            val alarmFile = savedList[index++] as String
                            alarms.add(Alarm(alarmType, alarmElevation, alarmFile))
                        }
                        
                        val altitudeUnit = UnitSystem.valueOf(savedList[index++] as String)
                        val altitudeStep = savedList[index++] as Int
                        
                        // Restore silence windows
                        val silenceWindowsSize = savedList[index++] as Int
                        val silenceWindows = mutableListOf<SilenceWindow>()
                        for (i in 0 until silenceWindowsSize) {
                            val top = savedList[index++] as Int
                            val bottom = savedList[index++] as Int
                            silenceWindows.add(SilenceWindow(top, bottom))
                        }
                        
                        // Restore navigation
                        val hasNavigation = savedList[index++] as Boolean
                        val navigation = if (hasNavigation) {
                            val deviceId = savedList[index++] as String
                            val lat = savedList[index++] as Int
                            val lon = savedList[index++] as Int
                            val bearing = savedList[index++] as Int
                            val endNav = savedList[index++] as Int
                            val maxDist = savedList[index++] as Int
                            val minAngle = savedList[index++] as Int
                            Navigation(deviceId, lat, lon, bearing, endNav, maxDist, minAngle)
                        } else defaultConfigFile().navigation.copy()
                        
                        // Restore ActiveLook
                        val activeLookDeviceId = savedList[index++] as String
                        val activeLookMode = ActiveLookMode.valueOf(savedList[index++] as String)
                        val activeLookRate = savedList[index++] as Int
                        val activeLookLinesSize = savedList[index++] as Int
                        val activeLookLines = mutableListOf<ActiveLookLine>()
                        for (i in 0 until activeLookLinesSize) {
                            val lineType = ActiveLookLineType.valueOf(savedList[index++] as String)
                            val unitSystem = UnitSystem.valueOf(savedList[index++] as String)
                            val decimal = savedList[index++] as Int
                            activeLookLines.add(ActiveLookLine(lineType, unitSystem, decimal))
                        }
                        
                        form.configFile = ConfigFile(
                            name = name,
                            description = description,
                            group = group,
                            dynamicModel = dynamicModel,
                            samplePeriod = samplePeriod,
                            toneMode = toneMode,
                            toneMinimum = toneMinimum,
                            toneMaximum = toneMaximum,
                            toneLimitBehaviour = toneLimitBehaviour,
                            toneVolume = toneVolume,
                            rateMode = rateMode,
                            rateMinimumValue = rateMinimumValue,
                            rateMaximumValue = rateMaximumValue,
                            rateMinimum = rateMinimum,
                            rateMaximum = rateMaximum,
                            flatLineAtMinimumRate = flatLineAtMinimumRate,
                            speechRate = speechRate,
                            speechVolume = speechVolume,
                            speeches = speeches,
                            verticalThreshold = verticalThreshold,
                            horizontalThreshold = horizontalThreshold,
                            tzOffset = tzOffset,
                            useSAS = useSAS,
                            initMode = initMode,
                            initFile = initFile,
                            windowAbove = windowAbove,
                            windowBelow = windowBelow,
                            dzElev = dzElev,
                            alarms = alarms,
                            altitudeUnit = altitudeUnit,
                            altitudeStep = altitudeStep,
                            silenceWindows = silenceWindows,
                            navigation = navigation,
                            activeLook = ActiveLook(activeLookDeviceId, activeLookMode, activeLookRate, activeLookLines)
                        )
                    } else {
                        form.configFile = null
                    }
                    
                    form.showMap = savedList[index++] as Boolean
                    form.displayPerformanceLane = savedList[index++] as Boolean
                    form.displayPerformanceLaneInMap = savedList[index++] as Boolean
                    
                    // Restore the whole ReferencePoint
                    val hasReferencePoint = savedList[index++] as Boolean
                    if (hasReferencePoint) {
                        val refId = savedList[index++] as String
                        val refName = savedList[index++] as String
                        val refDesc = savedList[index++] as String
                        val latitude = savedList[index++] as Double
                        val longitude = savedList[index++] as Double

                        val coords = Coordinate(latitude, longitude)
                        form.referencePoint = ReferencePoint(refId, refName, refDesc, coords)
                    } else {
                        form.referencePoint = null
                    }
                    
                    val displayGridName = savedList[index++] as String
                    form.displayGrid = DisplayGrid.valueOf(displayGridName)
                    
                    form.showGridLines = savedList[index++] as Boolean
                    
                    // Restore display items
                    val displayItemsSize = savedList[index++] as Int
                    val displayItems = mutableListOf<DisplayItem>()
                    
                    for (i in 0 until displayItemsSize) {
                        val capabilityName = savedList[index++] as String
                        val capability = DisplayableCapability.valueOf(capabilityName)
                        val caseIndex = savedList[index++] as Int
                        val indexInCase = savedList[index++] as Int
                        val hasBundle = savedList[index++] as Boolean
                        
                        var bundle: DisplayItemBundle? = null
                        if (hasBundle) {
                            val bundleType = savedList[index++] as String
                            when (bundleType) {
                                "DistanceToRefPointBundle" -> {
                                    // Restore ReferencePoint
                                    val refId = savedList[index++] as String
                                    val refName = savedList[index++] as String
                                    val refDesc = savedList[index++] as String
                                    val latitude = savedList[index++] as Double
                                    val longitude = savedList[index++] as Double
                                    
                                    val coords = Coordinate(latitude, longitude)
                                    val refPoint = ReferencePoint(refId, refName, refDesc, coords)
                                    bundle = DisplayItemBundle.DistanceToRefPointBundle(refPoint)
                                }
                            }
                        }
                        
                        displayItems.add(DisplayItem(
                            displayableCapability = capability,
                            caseIndex = caseIndex,
                            indexInCase = indexInCase,
                            bag = bundle
                        ))
                    }
                    
                    form.displayItems = displayItems
                    
                    form.useUSForTTS = savedList[index++] as Boolean
                    form.performanceLaneWidth = savedList[index++] as Int
                    form.competitionWindowTop = savedList[index++] as Int
                    form.competitionWindowBottom = savedList[index++] as Int
                    form.exitDetectionWindowTop = savedList[index++] as Int
                    form.exitDetectionWindowBottom = savedList[index++] as Int
                    form.showVisualAlertWhenExitDetected = savedList[index++] as Boolean
                    form.playAudioAlertWhenExitDetected = savedList[index++] as Boolean
                    form.showVisualAlertWhenNotInWindowBeforeExit = savedList[index++] as Boolean
                    form.exitPointsDown = savedList[index++] as Int
                    form.exitPointsUp = savedList[index++] as Int
                    form.exitDownThresh = savedList[index++] as Int
                    form.exitUpThresh = savedList[index++] as Int
                    form.exitDetectionConfirmationDuration = savedList[index++] as Long
                    form.timeAfterExit = savedList[index++] as Int
                    
                    // Add displayFlareDetector if available (might not be in older saved states)
                    if (index < savedList.size) {
                        form.displayFlareDetector = savedList[index++] as Boolean
                    }

                    form
                } catch (e: Exception) {
                    // Fallback to default if restoration fails
                    Timber.e(e, "Failed to restore SessionProfileForm")
                    SessionProfileForm()
                }
            }
        )
    }

    internal var isDirty by mutableStateOf(false)
    internal var hasValidProfileName by mutableStateOf(true)
    internal var isValid by mutableStateOf(true)

    internal var name by mutableStateOf<String?>(initialConfiguration.name)
    internal var description by mutableStateOf<String?>(initialConfiguration.description)

    internal var configFile by mutableStateOf<ConfigFile?>(initialConfiguration.configFile)
    internal var showMap by mutableStateOf(initialConfiguration.showMap)
    internal var displayPerformanceLane by mutableStateOf(initialConfiguration.showPerformanceLane)
    internal var displayPerformanceLaneInMap by mutableStateOf(initialConfiguration.showPerformanceLaneInMap)
    internal var referencePoint by mutableStateOf(initialConfiguration.referencePoint)
    internal var displayGrid by mutableStateOf(initialConfiguration.displayGrid)
    internal var showGridLines by mutableStateOf(initialConfiguration.showGridLines)
    internal var displayItems by mutableStateOf(initialConfiguration.displayItems)
    internal var useUSForTTS by mutableStateOf(initialConfiguration.useUSForTTS)
    
    internal var performanceLaneWidth by mutableStateOf(initialConfiguration.performanceLaneWidth)
    internal var competitionWindowTop by mutableStateOf(initialConfiguration.competitionWindowTop)
    internal var competitionWindowBottom by mutableStateOf(initialConfiguration.competitionWindowBottom)
    internal var exitDetectionWindowTop by mutableStateOf(initialConfiguration.exitDetectionWindowTop)
    internal var exitDetectionWindowBottom by mutableStateOf(initialConfiguration.exitDetectionWindowBottom)
    internal var showVisualAlertWhenExitDetected by mutableStateOf(initialConfiguration.showVisualAlertWhenExitDetected)
    internal var playAudioAlertWhenExitDetected by mutableStateOf(initialConfiguration.playAudioAlertWhenExitDetected)
    internal var showVisualAlertWhenNotInWindowBeforeExit by mutableStateOf(initialConfiguration.showVisualAlertWhenNotInWindowBeforeExit)

    internal var exitPointsDown by mutableStateOf(initialConfiguration.exitPointsDown)
    internal var exitPointsUp by mutableStateOf(initialConfiguration.exitPointsUp)
    internal var exitDownThresh by mutableStateOf(initialConfiguration.exitDownThresh)
    internal var exitUpThresh by mutableStateOf(initialConfiguration.exitUpThresh)
    internal var exitDetectionConfirmationDuration by mutableStateOf(initialConfiguration.exitDetectionConfirmationDuration)
    internal var timeAfterExit by mutableStateOf(initialConfiguration.timeAfterExit)
    internal var displayFlareDetector by mutableStateOf(initialConfiguration.displayFlareDetector)

    fun updateSessionProfileName(fileName: String) {
        name = fileName
        isDirty = true
        checkValidity()
        hasValidProfileName = fileName.isNotBlank()
    }

    fun updateProfileDescription(description: String) {
        this.description = description
        isDirty = true
        checkValidity()
    }

    fun updateConfigFile(configFile: ConfigFile?) {
        this.configFile = configFile
        isDirty = true
        checkValidity()
    }

    fun updateShowMap(showMap: Boolean) {
        this.showMap = showMap
        if (!showMap) {
            updateDisplayPerformanceLaneInMap(false)
        }
        isDirty = true
    }

    fun updateDisplayPerformanceLane(displayPerformanceLane: Boolean) {
        this.displayPerformanceLane = displayPerformanceLane
        isDirty = true
    }

    fun updateDisplayPerformanceLaneInMap(displayPerformanceLaneInMap: Boolean) {
        this.displayPerformanceLaneInMap = displayPerformanceLaneInMap
        isDirty = true
    }

    fun updateReferencePoint(referencePoint: ReferencePoint?) {
        this.referencePoint = referencePoint
        isDirty = true
        checkValidity()
    }

    fun updateDisplayGrid(displayGrid: DisplayGrid) {
        this.displayGrid = displayGrid
        isDirty = true
    }

    fun updateShowGridLines(showGridLines: Boolean) {
        this.showGridLines = showGridLines
        isDirty = true
    }


    fun updateDisplayItems(displayItems: List<DisplayItem>) {
        this.displayItems = displayItems
        isDirty = true
    }

    fun addDisplayItem(displayItem: DisplayItem) {
        this.displayItems += displayItem
        isDirty = true
    }
//
//    fun addDisplayableCapability(displayableCapability: DisplayableCapability) {
//        val firstAvailableCase = displayItems.groupBy { it.caseIndex }.firstNotNullOfOrNull { entry ->
//            if (entry.value.size < 3) entry.value else null
//        }
//        if (firstAvailableCase != null) {
//
//        }
//        val newDisplayItem = DisplayItem(
//            displayableCapability = displayableCapability,
//            caseIndex = 0,
//            indexInCase = 0
//        )
//        this.displayItems += newDisplayItem
//        isDirty = true
//    }

    fun removeDisplayItem(displayItem: DisplayItem) {
        this.displayItems -= displayItem
        isDirty = true
    }

    fun updateUseUSForTTS(useUSForTTS: Boolean) {
        this.useUSForTTS = useUSForTTS
        isDirty = true
    }
    
    fun updatePerformanceLaneWidth(width: Int) {
        this.performanceLaneWidth = width
        isDirty = true
    }
    
    fun updateCompetitionWindowTop(value: Int) {
        this.competitionWindowTop = value
        isDirty = true
    }
    
    fun updateCompetitionWindowBottom(value: Int) {
        this.competitionWindowBottom = value
        isDirty = true
    }
    
    fun updateExitDetectionWindowTop(value: Int) {
        this.exitDetectionWindowTop = value
        isDirty = true
    }
    
    fun updateExitDetectionWindowBottom(value: Int) {
        this.exitDetectionWindowBottom = value
        isDirty = true
    }
    
    fun updateShowVisualAlertWhenExitDetected(show: Boolean) {
        this.showVisualAlertWhenExitDetected = show
        isDirty = true
    }
    
    fun updatePlayAudioAlertWhenExitDetected(play: Boolean) {
        this.playAudioAlertWhenExitDetected = play
        isDirty = true
    }
    
    fun updateShowVisualAlertWhenNotInWindowBeforeExit(show: Boolean) {
        this.showVisualAlertWhenNotInWindowBeforeExit = show
        isDirty = true
    }

    fun updateExitPointsDown(value: Int) {
        this.exitPointsDown = value
        isDirty = true
    }
    
    fun updateExitPointsUp(value: Int) {
        this.exitPointsUp = value
        isDirty = true
    }
    
    fun updateExitDownThresh(value: Int) {
        this.exitDownThresh = value
        isDirty = true
    }
    
    fun updateExitUpThresh(value: Int) {
        this.exitUpThresh = value
        isDirty = true
    }
    
    fun updateExitDetectionConfirmationDuration(value: Long) {
        this.exitDetectionConfirmationDuration = value
        isDirty = true
    }

    fun updateTimeAfterExit(value: Int) {
        this.timeAfterExit = value
        isDirty = true
    }

    fun updateDisplayFlareDetector(value: Boolean) {
        this.displayFlareDetector = value
        isDirty = true
    }

    private fun checkValidity() {
        isValid =
            name != null &&
                    description != null &&
                    configFile != null &&
                    referencePoint != null
    }

    fun toSessionConfiguration(): SessionProfile? {
        return SessionProfile(
            name = name ?: return null,
            description = description ?: return null,
            sessionType = SessionType.Hud,
            configFile = configFile ?: return null,
            showMap = showMap,
            showPerformanceLane = displayPerformanceLane,
            showPerformanceLaneInMap = displayPerformanceLaneInMap,
            performanceLaneWidth = performanceLaneWidth,
            competitionWindowTop = competitionWindowTop,
            competitionWindowBottom = competitionWindowBottom,
            exitDetectionWindowTop = exitDetectionWindowTop,
            exitDetectionWindowBottom = exitDetectionWindowBottom,
            exitPointsDown = exitPointsDown,
            exitPointsUp = exitPointsUp,
            exitDownThresh = exitDownThresh,
            exitUpThresh = exitUpThresh,
            exitDetectionConfirmationDuration = exitDetectionConfirmationDuration,
            timeAfterExit = timeAfterExit,
            showVisualAlertWhenExitDetected = showVisualAlertWhenExitDetected,
            playAudioAlertWhenExitDetected = playAudioAlertWhenExitDetected,
            showVisualAlertWhenNotInWindowBeforeExit = showVisualAlertWhenNotInWindowBeforeExit,
            referencePoint = referencePoint,
            displayGrid = displayGrid,
            showGridLines = showGridLines,
            useUSForTTS = useUSForTTS,
            displayItems = displayItems,
            displayFlareDetector = displayFlareDetector
        )
    }
}
