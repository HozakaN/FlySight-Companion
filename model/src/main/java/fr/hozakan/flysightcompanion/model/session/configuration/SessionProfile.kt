package fr.hozakan.flysightcompanion.model.session.configuration

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DisplayableConfig
import fr.hozakan.flysightcompanion.model.defaultConfigFile

data class SessionProfile(
    val profileVersion: Int = LATEST_PROFILE_VERSION,
    override val name: String,
    val description: String,
    val sessionType: SessionType,
    val configFile: ConfigFile,
    val showMap: Boolean,
    val showPerformanceLane: Boolean,
    val showPerformanceLaneInMap: Boolean,
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
