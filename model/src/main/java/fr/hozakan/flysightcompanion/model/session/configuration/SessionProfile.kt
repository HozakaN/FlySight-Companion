package fr.hozakan.flysightcompanion.model.session.configuration

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DisplayableConfig
import fr.hozakan.flysightcompanion.model.defaultConfigFile
import kotlin.Int

data class SessionProfile(
    val profileVersion: Int = LATEST_PROFILE_VERSION,
    override val name: String,
    val description: String,
    val sessionType: SessionType,
    val configFile: ConfigFile,
    val showMap: Boolean,
    val showPerformanceLane: Boolean,
    val showPerformanceLaneInMap: Boolean,
    val performanceLaneWidth: Int,
    val competitionWindowTop: Int,
    val competitionWindowBottom: Int,
    val exitDetectionWindowTop: Int, // Do not detect an exit if above this altitude
                                    // (negative to disable this check)
    val exitDetectionWindowBottom: Int, //Do not detect an exit if below this altitude
                                        // (negative to disable exit detection)
    val exitPointsDown: Int, // Consecutive points down to indicate an exit
    val exitPointsUp: Int, //  Consecutive points up to reset the exit altitude
    val exitDownThresh: Int, // Speed (cm/s) to indicate down (positive) (initialize exit altitude)
    val exitUpThresh: Int, // Speed (cm/s) to indicate up (negative) (reset exit altitude)
    val showVisualAlertWhenExitDetected: Boolean,
    val playAudioAlertWhenExitDetected: Boolean,
    val showVisualAlertWhenNotInWindowBeforeExit: Boolean,
    val referencePoint: ReferencePoint?,
    val displayGrid: DisplayGrid,
    val showGridLines: Boolean,
    val displayItems: List<DisplayItem>,
    val useUSForTTS: Boolean
) : DisplayableConfig {

    companion object {
        private const val LATEST_PROFILE_VERSION: Int = 1
        fun default() = SessionProfile(
            name = "",
            description = "",
            sessionType = SessionType.Visual,
            configFile = defaultConfigFile(),
            showMap = false,
            showPerformanceLane = true,
            showPerformanceLaneInMap = false,
            performanceLaneWidth = 200,
            competitionWindowTop = 2500,
            competitionWindowBottom = 1500,
            exitDetectionWindowTop = 3350,
            exitDetectionWindowBottom = 3200,
            exitPointsDown = 5,
            exitPointsUp = 50,
            exitDownThresh = 800,
            exitUpThresh = -800,
            showVisualAlertWhenExitDetected = true,
            playAudioAlertWhenExitDetected = false,
            showVisualAlertWhenNotInWindowBeforeExit = false,
            referencePoint = null,
            displayGrid = DisplayGrid.InlineLeft,
            showGridLines = true,
            useUSForTTS = true,
            displayItems = listOf(
                DisplayItem(
                    displayableCapability = DisplayableCapability.Altitude,
                    caseIndex = 0,
                    indexInCase = 0
                ),
                DisplayItem(
                    displayableCapability = DisplayableCapability.Elevation,
                    caseIndex = 0,
                    indexInCase = 1
                ),
                DisplayItem(
                    displayableCapability = DisplayableCapability.Latitude,
                    caseIndex = 0,
                    indexInCase = 2
                ),
                DisplayItem(
                    displayableCapability = DisplayableCapability.Longitude,
                    caseIndex = 0,
                    indexInCase = 3
                ),
            )
        )
    }
}
