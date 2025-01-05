package fr.hozakan.flysightcompanion.userpreferencesmodule

import fr.hozakan.flysightcompanion.model.config.UnitSystem
import kotlinx.coroutines.flow.StateFlow

interface UserPrefService {
    val unitSystem: StateFlow<UnitSystem>
    val showConfigAsRaw: StateFlow<Boolean>
    fun updateUnitSystem(unitSystem: UnitSystem)
    fun updateShowConfigAsRaw(showConfigAsRaw: Boolean)
}