package fr.hozakan.flysightcompanion.model.session.configuration

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DisplayableConfig
import fr.hozakan.flysightcompanion.model.defaultConfigFile

data class SessionProfile(
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
    val displayItems: List<DisplayItem>
) : DisplayableConfig {
    companion object {
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
