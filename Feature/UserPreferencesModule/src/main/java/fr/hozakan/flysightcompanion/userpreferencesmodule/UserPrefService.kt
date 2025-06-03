package fr.hozakan.flysightcompanion.userpreferencesmodule

import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.ui.PlotBottomItem
import fr.hozakan.flysightcompanion.model.ui.PlotDisplayPreference
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem
import kotlinx.coroutines.flow.StateFlow

interface UserPrefService {
    val unitSystem: StateFlow<UnitSystem>
    val showConfigAsRaw: StateFlow<Boolean>
    val plotLeftItems: StateFlow<List<PlotLeftItem>>
    val plotBottomItem: StateFlow<PlotBottomItem>
    val plotDisplayPreferences: StateFlow<List<PlotDisplayPreference>>
    val planeDisplayDzElev: StateFlow<Int>
    val planeDisplayColorBlindOption: StateFlow<Boolean>
    fun updatePlaneDisplayColorBlindOption(planeDisplayColorBlindOption: Boolean)

    /**
     * 0 for performance, 1 for acrobatics
     */
    val planeDisplayDiscipline: StateFlow<Int>
    fun updatePlaneDisplayDiscipline(planeDisplayDiscipline: Int)
    fun updatePlaneDisplayDzElev(planeDisplayDzElev: Int)
    fun updateUnitSystem(unitSystem: UnitSystem)
    fun updateShowConfigAsRaw(showConfigAsRaw: Boolean)
    fun updatePlotLeftItems(plotLeftItems: List<PlotLeftItem>)
    fun updatePlotBottomItem(plotBottomItem: PlotBottomItem)
    fun updatePlotDisplayPreferences(plotDisplayPreferences: List<PlotDisplayPreference>)
    suspend fun canShowFirmwareWarningForVersion(deviceId: String, firmwareVersionName: String): Boolean
    fun updateFirmwareWarningForDeviceIdAndFirmwareVersion(deviceId: String, firmwareVersionName: String)
}