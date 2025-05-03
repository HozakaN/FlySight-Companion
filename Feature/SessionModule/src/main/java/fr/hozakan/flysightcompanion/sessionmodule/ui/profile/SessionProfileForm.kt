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

    private fun checkValidity() {
        isValid =
            name != null &&
                    description != null &&
                    configFile != null
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
            referencePoint = referencePoint,
            displayGrid = displayGrid,
            showGridLines = showGridLines,
            displayItems = displayItems
        )
    }
}