package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayGrid
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItem
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionType

@Composable
fun rememberSessionProfileForm(
    initialConfiguration: SessionProfile = SessionProfile.default()
): SessionProfileForm {
    return remember(initialConfiguration) {
        SessionProfileForm(initialConfiguration)
    }
}

@Stable
class SessionProfileForm(
    initialConfiguration: SessionProfile = SessionProfile.default()
) {

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
    internal var timeAfterExit by mutableStateOf(initialConfiguration.timeAfterExit)

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

    fun addDisplayItem(displayItem: DisplayItem) {
        this.displayItems += displayItem
        isDirty = true
    }

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

    fun updateTimeAfterExit(value: Int) {
        this.timeAfterExit = value
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
            sessionType = SessionType.Visual,
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
            timeAfterExit = timeAfterExit,
            showVisualAlertWhenExitDetected = showVisualAlertWhenExitDetected,
            playAudioAlertWhenExitDetected = playAudioAlertWhenExitDetected,
            showVisualAlertWhenNotInWindowBeforeExit = showVisualAlertWhenNotInWindowBeforeExit,
            referencePoint = referencePoint,
            displayGrid = displayGrid,
            showGridLines = showGridLines,
            useUSForTTS = useUSForTTS,
            displayItems = displayItems
        )
    }
}
