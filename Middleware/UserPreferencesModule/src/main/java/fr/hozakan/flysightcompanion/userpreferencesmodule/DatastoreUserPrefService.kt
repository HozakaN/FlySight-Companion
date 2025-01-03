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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
        produceFile = { appContext.preferencesDataStoreFile("fr.hozakan.flusightble.prefs") }
    )

    override val unitSystem: StateFlow<UnitSystem>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Int>("unit-system")
                val value = preferences[key] ?: UnitSystem.Metric.value
                UnitSystem.fromValue(value) ?: UnitSystem.Metric
            }
            .stateIn(dataStoreCoroutineScope, SharingStarted.WhileSubscribed(), UnitSystem.Metric)

    override val showConfigAsRaw: StateFlow<Boolean>
        get() = dataStore.data
            .map { preferences ->
                val key = getKey<Boolean>("show_config_as_raw")
                val value = preferences[key] ?: false
                value
            }
            .stateIn(dataStoreCoroutineScope, SharingStarted.WhileSubscribed(), false)

    override fun updateUnitSystem(unitSystem: UnitSystem) {
        dataStoreCoroutineScope.launch {
            val key = getKey<Int>("unit-system")
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
