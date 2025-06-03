package fr.hozakan.flysightcompanion.userpreferencesmodule

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.ui.PlotBottomItem
import fr.hozakan.flysightcompanion.model.ui.PlotDisplayPreference
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem
import fr.hozakan.flysightcompanion.model.ui.toStringPreference
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class DatastoreUserPrefService(
    appContext: Context
) : UserPrefService {

    private val dataStoreCoroutineScope = CoroutineScope(
        Dispatchers.IO +
                SupervisorJob() +
                CoroutineExceptionHandler { _, throwable ->
                    Timber.e(throwable)
                }
    )

    private val dataStore = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { emptyPreferences() }),
        migrations = listOf(SharedPreferencesMigration(appContext, "fr.hozakan.flusightble.prefs")),
        scope = dataStoreCoroutineScope,
        produceFile = { appContext.preferencesDataStoreFile("fr.hozakan.flysight.companion.prefs") }
    )

    override val unitSystem: StateFlow<UnitSystem>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Int>("unit_system")
                val value = preferences[key] ?: UnitSystem.Metric.value
                UnitSystem.fromValue(value) ?: UnitSystem.Metric
            }
            .stateIn(dataStoreCoroutineScope, SharingStarted.WhileSubscribed(), UnitSystem.Metric)

    override val showConfigAsRaw: StateFlow<Boolean>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Boolean>("show_config_as_raw")
                preferences[key] ?: false
            }
            .stateIn(dataStoreCoroutineScope, SharingStarted.WhileSubscribed(), false)

    override val plotLeftItems: StateFlow<List<PlotLeftItem>>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Set<String>>("plot_left_items")
                val value = preferences[key] ?: setOf(PlotLeftItem.Elevation.name)
                value.mapNotNull { PlotLeftItem.fromName(it) }
            }
            .stateIn(
                dataStoreCoroutineScope,
                SharingStarted.WhileSubscribed(),
                listOf(PlotLeftItem.Elevation)
            )

    override val plotBottomItem: StateFlow<PlotBottomItem>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<String>("plot_bottom_item")
                val value = preferences[key] ?: PlotBottomItem.Time.name
                PlotBottomItem.fromString(value)
            }
            .filterNotNull()
            .stateIn(
                dataStoreCoroutineScope,
                SharingStarted.WhileSubscribed(),
                PlotBottomItem.Time
            )

    override val plotDisplayPreferences: StateFlow<List<PlotDisplayPreference>>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<String>("plot_display_preferences")
                val value = preferences[key]
                value?.let { prefs ->
                    PlotDisplayPreference.fromStringPreference(prefs)
                } ?: PlotDisplayPreference.defaultValues()
            }
            .filterNotNull()
            .stateIn(
                dataStoreCoroutineScope,
                SharingStarted.WhileSubscribed(),
                PlotDisplayPreference.defaultValues()
            )

    override val planeDisplayDzElev: StateFlow<Int>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Int>("plane_display_dz_elev")
                preferences[key] ?: 0 // Default value in meters
            }
            .stateIn(dataStoreCoroutineScope, SharingStarted.WhileSubscribed(), 0)

    override val planeDisplayColorBlindOption: StateFlow<Boolean>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Boolean>("plane_display_color_blind_option")
                preferences[key] ?: false // Default value: false
            }
            .stateIn(dataStoreCoroutineScope, SharingStarted.WhileSubscribed(), false)

    override fun updatePlaneDisplayColorBlindOption(planeDisplayColorBlindOption: Boolean) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Boolean>("plane_display_color_blind_option")
            dataStore.edit { preferences ->
                preferences[key] = planeDisplayColorBlindOption
            }
        }
    }

    override val planeDisplayDiscipline: StateFlow<Int>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Int>("plane_display_discipline")
                preferences[key] ?: 0 // Default value: 0 for performance, 1 for acrobatics
            }
            .stateIn(dataStoreCoroutineScope, SharingStarted.WhileSubscribed(), 0)

    override fun updatePlaneDisplayDiscipline(planeDisplayDiscipline: Int) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Int>("plane_display_discipline")
            dataStore.edit { preferences ->
                preferences[key] = planeDisplayDiscipline
            }
        }
    }

    override fun updatePlaneDisplayDzElev(planeDisplayDzElev: Int) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Int>("plane_display_dz_elev")
            dataStore.edit { preferences ->
                preferences[key] = planeDisplayDzElev
            }
        }
    }

    override fun updateUnitSystem(unitSystem: UnitSystem) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Int>("unit_system")
            dataStore.edit { preferences ->
                preferences[key] = unitSystem.value
            }
        }
    }

    override fun updateShowConfigAsRaw(showConfigAsRaw: Boolean) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Boolean>("show_config_as_raw")
            dataStore.edit { preferences ->
                preferences[key] = showConfigAsRaw
            }
        }
    }

    override fun updatePlotLeftItems(plotLeftItems: List<PlotLeftItem>) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Set<String>>("plot_left_items")
            dataStore.edit { preferences ->
                preferences[key] = plotLeftItems.map { it.name }.toSet()
            }
        }
    }

    override fun updatePlotBottomItem(plotBottomItem: PlotBottomItem) {
        dataStoreCoroutineScope.launch {
            val key = getKey<String>("plot_bottom_item")
            dataStore.edit { preferences ->
                preferences[key] = plotBottomItem.name
            }
        }
    }

    override fun updatePlotDisplayPreferences(plotDisplayPreferences: List<PlotDisplayPreference>) {
        updateUnitSystem(unitSystem = if (unitSystem.value == UnitSystem.Metric) UnitSystem.Imperial else UnitSystem.Metric)
        dataStoreCoroutineScope.launch {
            val key = getKey<String>("plot_display_preferences")
            dataStore.edit { preferences ->
                preferences[key] = plotDisplayPreferences.toStringPreference()
            }
        }
    }

    override suspend fun canShowFirmwareWarningForVersion(
        deviceId: String,
        firmwareVersionName: String
    ): Boolean {
        val key =
            getKey<Boolean>("can_show_firmware_warning_for_version_${deviceId}_$firmwareVersionName")
        return dataStore.data.first()[key] ?: true
    }

    override fun updateFirmwareWarningForDeviceIdAndFirmwareVersion(
        deviceId: String,
        firmwareVersionName: String
    ) {
        dataStoreCoroutineScope.launch {
            val key =
                getKey<Boolean>("can_show_firmware_warning_for_version_${deviceId}_$firmwareVersionName")
            dataStore.edit { preferences ->
                preferences[key] = false
            }
        }
    }

    /*
     override fun updateUnitSystem(unitSystem: UnitSystem) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Int>("unit_system")
            dataStore.edit { preferences ->
                preferences[key] = unitSystem.value
            }
        }
    }
     */

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> getKey(name: String): Preferences.Key<T> {
        return when (T::class) {
            Integer::class -> intPreferencesKey(name)
            Long::class -> longPreferencesKey(name)
            Double::class -> doublePreferencesKey(name)
            Float::class -> floatPreferencesKey(name)
            Boolean::class -> booleanPreferencesKey(name)
            String::class -> stringPreferencesKey(name)
            // /!\ If a Set of something else than Strings is used
            // /!\ it will crash when getting/setting the value from/to the Preferences object
            Set::class -> stringSetPreferencesKey(name)
            else -> throw Exception("Class \"${T::class.java.name}\" unsupported by data store (key=\"$name\")")
        } as Preferences.Key<T>
    }

}

//fun dataStore(keyName: String, defaultValue: Int): ReadOnlyProperty<String, StateFlow<Int>> =
//    ReadOnlyProperty<String, StateFlow<Int>> { thisRef, property -> TODO("Not yet implemented") }
//    return ReadOnlyProperty { thisRef, property ->
//        flowOf(5).stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(), 5)
//    }
