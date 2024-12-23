package fr.hozakan.flysightble.userpreferencesmodule

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Service that provides delegates to access DataStore properties.
 * It must be initialized prior to use its delegates.
 * Supported types are: Int, Double, String, Boolean, Float, Long, Set<String>
 * and Serializable classes (kotlinx.serializable.json) by using KSerializer<T>
 *
 * Usage:
 * private var dataStoreProperty by dataStore("KEY_NAME", defaultValue)
 * private var dataStoreOptProperty by dataStoreOpt<Type>("KEY_NAME", defaultValue)
 * private var dataStoreSerializableProperty by dataStore(MyClass.serializer(), "KEY_NAME", MyClass())
 * private var dataStoreSerializableOptProperty by dataStoreOpt(MyClass.serializer(), "KEY_NAME")
 * private var dataStorePropertyAccessor by dataStoreAccessor("KEY_NAME", defaultValue)
 * private var dataStoreOptPropertyAccessor by dataStoreOptAccessor<Type>("KEY_NAME", defaultValue)
 * private var dataStoreSerializablePropertyAccessor by dataStoreAccessor(MyClass.serializer(), "KEY_NAME", MyClass())
 * private var dataStoreSerializableOptPropertyAccessor by dataStoreOptAccessor(MyClass.serializer(), "KEY_NAME", defaultValue)
 */
object DataStoreService {

    lateinit var dataStore: DataStore<Preferences>
        private set

    val dataStoreCoroutineScope = CoroutineScope(
        Dispatchers.IO +
                SupervisorJob() +
                CoroutineExceptionHandler { _, throwable ->
                    Timber.e(throwable)
                }
    )

    fun init(appContext: Context) {
        val preferencesDataStoreName = "preferences"
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { emptyPreferences() }),
            migrations = listOf(SharedPreferencesMigration(appContext, preferencesDataStoreName)),
            scope = dataStoreCoroutineScope,
            produceFile = { appContext.preferencesDataStoreFile(preferencesDataStoreName) }
        )
        dataStoreCoroutineScope.launch {
            // Preload
            dataStore.data.first()
        }
        DataStoreService.dataStore = dataStore
    }

    //region Standard types management

    /** Get preferences key from generic type */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getKey(name: String): Preferences.Key<T> {
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

    inline fun <reified T> getFlow(keyName: String, defaultValue: T): Flow<T> {
        val key = getKey<T>(keyName)
        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    inline fun <reified T> getValue(keyName: String, defaultValue: T): T {
        val key = getKey<T>(keyName)
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[key] ?: defaultValue
            }.first()
        }
    }

    inline fun <reified T> setValue(keyName: String, value: T) {
        val key = getKey<T>(keyName)
        dataStoreCoroutineScope.launch {
            dataStore.edit { settings ->
                if (value == null) settings.remove(key)
                else settings[key] = value
            }
        }
    }

    suspend inline fun <reified T> suspendSetValue(keyName: String, value: T): Result<Unit> {
//        return suspendCatching {
            val key = getKey<T>(keyName)
            dataStore.edit { settings ->
                if (value == null) settings.remove(key)
                else settings[key] = value
            }
//        }
        return Result.success(Unit)
    }

    /** Get accessor delegate for specified key name and type */
    inline fun <reified T> getAccessorDelegate(
        keyName: String,
        defaultValue: T
    ): ReadOnlyProperty<Any?, DataStoreProperty<T>> {
        return ReadOnlyProperty { _, _ ->
            DataStoreProperty.init(keyName, defaultValue)
        }
    }
    /** Get accessor delegate for specified key name and type */
    inline fun <reified T> getMutableAccessorDelegate(
        keyName: String,
        defaultValue: T
    ): ReadWriteProperty<Any?, DataStoreProperty3<T>> {
        return object : ReadWriteProperty<Any?, DataStoreProperty3<T>> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): DataStoreProperty3<T> {
                return DataStoreProperty.init3(keyName, defaultValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: DataStoreProperty3<T>) {
                value.value = value.value
            }
        }
    }

    /** Get accessor delegate for specified key name and type */
    inline fun <reified T> getAccessorDelegate2(
        keyName: String,
        defaultValue: T
    ): ReadOnlyProperty<Any?, DataStoreProperty2<T>> {
        return ReadOnlyProperty { _, _ ->
            DataStoreProperty.init2(keyName, defaultValue)
        }
    }

    /** Get accessor delegate for specified key name and type */
    inline fun <reified T> getAccessorDelegate3(
        keyName: String,
        defaultValue: T
    ): ReadOnlyProperty<Any?, DataStoreProperty3<T>> {
        return ReadOnlyProperty { _, _ ->
            DataStoreProperty.init3(keyName, defaultValue)
        }
    }
    //endregion

//    //region Serializable non optional types management
//
//    inline fun <reified T> getSerializableFlow(
//        serializer: KSerializer<T>,
//        keyName: String,
//        defaultValue: T
//    ): Flow<T> {
//        val key = stringPreferencesKey(keyName)
//        return dataStore.data.map { preferences ->
//            val str = preferences[key]
//            if (str != null) {
//                syncCatching {
//                    json.decodeFromString(serializer, str)
//                }.onFailure {
//                    Timber.e(it, "Failed to decode datastore entry $key")
//                }.getOrNull() ?: defaultValue
//            } else defaultValue
//        }
//    }
//
//    fun <T> getValue(
//        serializer: KSerializer<T>,
//        keyName: String,
//        defaultValue: T
//    ): T {
//        val key = stringPreferencesKey(keyName)
//        return runBlocking {
//            dataStore.data.map { preferences ->
//                val str = preferences[key]
//                if (str != null) {
//                    syncCatching {
//                        json.decodeFromString(serializer, str)
//                    }.onFailure {
//                        Timber.e(it, "Failed to decode datastore entry $key")
//                    }.getOrNull() ?: defaultValue
//                } else defaultValue
//            }.first()
//        }
//    }
//
//    fun <T> setValue(
//        serializer: KSerializer<T>,
//        keyName: String,
//        value: T
//    ) {
//        val key = stringPreferencesKey(keyName)
//        dataStoreCoroutineScope.launch {
//            dataStore.edit { settings ->
//                if (value == null) settings.remove(key)
//                else settings[key] = json.encodeToString(serializer, value)
//            }
//        }
//    }
//
//    suspend fun <T> suspendSetValue(
//        serializer: KSerializer<T>,
//        keyName: String,
//        value: T
//    ): Result<Unit> {
//        return suspendCatching {
//            val key = stringPreferencesKey(keyName)
//            dataStore.edit { settings ->
//                if (value == null) settings.remove(key)
//                else settings[key] = json.encodeToString(serializer, value)
//            }
//        }
//    }
//
//    /** Get accessor delegate for specified key name and serializer */
//    inline fun <reified T> getSerializableAccessorDelegate(
//        serializer: KSerializer<T>,
//        keyName: String,
//        defaultValue: T
//    ): ReadOnlyProperty<Any?, DataStoreProperty<T>> {
//        return ReadOnlyProperty { _, _ ->
//            DataStoreProperty.initSerializable(serializer, keyName, defaultValue)
//        }
//    }
//
//    //endregion
//
//    //region Serializable optional types management
//
//    inline fun <reified T> getSerializableOptFlow(
//        serializer: KSerializer<T>,
//        keyName: String,
//        defaultValue: T?
//    ): Flow<T?> {
//        val key = stringPreferencesKey(keyName)
//        return dataStore.data.map { preferences ->
//            val str = preferences[key]
//            if (str != null) {
//                syncCatching {
//                    json.decodeFromString(serializer, str)
//                }.onFailure {
//                    Timber.e(it, "Failed to decode datastore entry $key")
//                }.getOrNull() ?: defaultValue
//            } else defaultValue
//        }
//    }
//
//    fun <T> getOptValue(
//        serializer: KSerializer<T>,
//        keyName: String,
//        defaultValue: T?
//    ): T? {
//        val key = stringPreferencesKey(keyName)
//        return runBlocking {
//            dataStore.data.map { preferences ->
//                val str = preferences[key]
//                if (str != null) {
//                    syncCatching {
//                        json.decodeFromString(serializer, str)
//                    }.onFailure {
//                        Timber.e(it, "Failed to decode datastore entry $key")
//                    }.getOrNull() ?: defaultValue
//                } else defaultValue
//            }.first()
//        }
//    }
//
//    fun <T> setOptValue(
//        serializer: KSerializer<T>,
//        keyName: String,
//        value: T?
//    ) {
//        val key = stringPreferencesKey(keyName)
//        dataStoreCoroutineScope.launch {
//            dataStore.edit { settings ->
//                if (value == null) settings.remove(key)
//                else settings[key] = json.encodeToString(serializer, value)
//            }
//        }
//    }
//
//    suspend fun <T> suspendSetOptValue(
//        serializer: KSerializer<T>,
//        keyName: String,
//        value: T?
//    ): Result<Unit> {
//        return suspendCatching {
//            val key = stringPreferencesKey(keyName)
//            dataStore.edit { settings ->
//                if (value == null) settings.remove(key)
//                else settings[key] = json.encodeToString(serializer, value)
//            }
//        }
//    }
//
//    /** Get opt accessor delegate for specified key name and serializer */
//    inline fun <reified T> getSerializableOptAccessorDelegate(
//        serializer: KSerializer<T>,
//        keyName: String,
//        defaultValue: T?
//    ): ReadOnlyProperty<Any?, DataStoreProperty<T?>> {
//        return ReadOnlyProperty { _, _ ->
//            DataStoreProperty.initSerializableOpt(serializer, keyName, defaultValue)
//        }
//    }
//
//    //endregion
}

//region Accessor
fun dataStore(keyName: String, defaultValue: Int): ReadOnlyProperty<Any?, DataStoreProperty<Int>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)
fun dataStoreState(keyName: String, defaultValue: Int): ReadWriteProperty<Any?, DataStoreProperty3<Int>> =
    DataStoreService.getMutableAccessorDelegate(keyName, defaultValue)
fun dataStore2(keyName: String, defaultValue: Int): ReadOnlyProperty<Any?, DataStoreProperty2<Int>> =
    DataStoreService.getAccessorDelegate2(keyName, defaultValue)
fun dataStore3(keyName: String, defaultValue: Int): ReadOnlyProperty<Any?, DataStoreProperty3<Int>> =
    DataStoreService.getAccessorDelegate3(keyName, defaultValue)

fun dataStore(keyName: String, defaultValue: Long): ReadOnlyProperty<Any?, DataStoreProperty<Long>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

fun dataStore(keyName: String, defaultValue: Double): ReadOnlyProperty<Any?, DataStoreProperty<Double>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

fun dataStore(keyName: String, defaultValue: Float): ReadOnlyProperty<Any?, DataStoreProperty<Float>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

fun dataStore(
    keyName: String,
    defaultValue: Boolean
): ReadOnlyProperty<Any?, DataStoreProperty<Boolean>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

fun dataStore(keyName: String, defaultValue: String): ReadOnlyProperty<Any?, DataStoreProperty<String>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)
fun dataStoreState(keyName: String, defaultValue: String): ReadOnlyProperty<Any?, DataStoreProperty2<String>> =
    DataStoreService.getAccessorDelegate2(keyName, defaultValue)

fun dataStore(
    keyName: String,
    defaultValue: Set<String>
): ReadOnlyProperty<Any?, DataStoreProperty<Set<String>>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

//inline fun <reified T> dataStore(
//    serializer: KSerializer<T>,
//    keyName: String,
//    defaultValue: T
//): ReadOnlyProperty<Any?, DataStoreProperty<T>> =
//    DataStoreService.getSerializableAccessorDelegate(serializer, keyName, defaultValue)

//endregion

//region Opt Accessor
@Suppress("FINAL_UPPER_BOUND")
inline fun <reified T : Int> dataStoreOpt(
    keyName: String,
    defaultValue: T? = null
): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

@Suppress("FINAL_UPPER_BOUND")
inline fun <reified T : Long> dataStoreOpt(
    keyName: String,
    defaultValue: T? = null
): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

@Suppress("FINAL_UPPER_BOUND")
inline fun <reified T : Double> dataStoreOpt(
    keyName: String,
    defaultValue: T? = null
): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

@Suppress("FINAL_UPPER_BOUND")
inline fun <reified T : Float> dataStoreOpt(
    keyName: String,
    defaultValue: T? = null
): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

@Suppress("FINAL_UPPER_BOUND")
inline fun <reified T : Boolean> dataStoreOpt(
    keyName: String,
    defaultValue: T? = null
): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

@Suppress("FINAL_UPPER_BOUND")
inline fun <reified T : String> dataStoreOpt(
    keyName: String,
    defaultValue: T? = null
): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

inline fun <reified T : Set<String>> dataStoreOpt(
    keyName: String,
    defaultValue: T? = null
): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
    DataStoreService.getAccessorDelegate(keyName, defaultValue)

//inline fun <reified T> dataStoreOpt(
//    serializer: KSerializer<T>,
//    keyName: String,
//    defaultValue: T? = null
//): ReadOnlyProperty<Any?, DataStoreProperty<T?>> =
//    DataStoreService.getSerializableOptAccessorDelegate(serializer, keyName, defaultValue)

//endregion

//region Model
class DataStoreProperty3<T>(
    private val flow: Flow<T>,
    private val getValue: () -> T,
    private val setValue: (T) -> Unit,
    private val suspendSetValue: suspend (T) -> Result<Unit>,
) : MutableState<T> {

    private var _value: T = getValue()

    override var value: T
        get() = getValue()
        set(value) {
            setValue(value)
        }

    override fun component1(): T {
        return getValue()
    }

    override fun component2(): (T) -> Unit = {
        setValue(it)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setValue(value)
    }

}

class DataStoreProperty2<T>(
    private val flow: Flow<T>,
    private val getValue: () -> T,
    private val setValue: (T) -> Unit,
    private val suspendSetValue: suspend (T) -> Result<Unit>,
) : State<T> {
    override val value: T
        get() = getValue()

    suspend fun set(value: T): Result<Unit> {
        return suspendSetValue(value)
    }
}

class DataStoreProperty<T>(
    private val flow: Flow<T>,
    private val getValue: () -> T,
    private val setValue: (T) -> Unit,
    private val suspendSetValue: suspend (T) -> Result<Unit>,
) : StateFlow<T> {
    companion object;

    override val replayCache: List<T>
        get() = listOf(value)

    override val value: T
        get() = getValue()

    fun setLater(value: T) {
        setValue(value)
    }

    fun updateLater(function: (T) -> T) {
        setLater(function(value))
    }

    suspend fun set(value: T): Result<Unit> {
        return suspendSetValue(value)
    }

    suspend fun update(function: (T) -> T) {
        set(function(value))
    }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        coroutineScope { flow.distinctUntilChanged().stateIn(this).collect(collector) }
    }
}

inline fun <reified T> DataStoreProperty.Companion.init(
    cacheKey: String,
    initPath: T
) = DataStoreProperty(
    flow = DataStoreService.getFlow(cacheKey, initPath),
    getValue = { DataStoreService.getValue(cacheKey, initPath) },
    setValue = { DataStoreService.setValue(cacheKey, it) },
    suspendSetValue = { DataStoreService.suspendSetValue(cacheKey, it) }
)

inline fun <reified T> DataStoreProperty.Companion.init2(
    cacheKey: String,
    initPath: T
) = DataStoreProperty2(
    flow = DataStoreService.getFlow(cacheKey, initPath),
    getValue = { DataStoreService.getValue(cacheKey, initPath) },
    setValue = { DataStoreService.setValue(cacheKey, it) },
    suspendSetValue = { DataStoreService.suspendSetValue(cacheKey, it) }
)

inline fun <reified T> DataStoreProperty.Companion.init3(
    cacheKey: String,
    initPath: T
) = DataStoreProperty3(
    flow = DataStoreService.getFlow(cacheKey, initPath),
    getValue = { DataStoreService.getValue(cacheKey, initPath) },
    setValue = { DataStoreService.setValue(cacheKey, it) },
    suspendSetValue = { DataStoreService.suspendSetValue(cacheKey, it) }
)

//inline fun <reified T> DataStoreProperty.Companion.initSerializable(
//    serializer: KSerializer<T>,
//    cacheKey: String,
//    initPath: T
//) = DataStoreProperty(
//    flow = DataStoreService.getSerializableFlow(serializer, cacheKey, initPath),
//    getValue = { DataStoreService.getValue(serializer, cacheKey, initPath) },
//    setValue = { DataStoreService.setValue(serializer, cacheKey, it) },
//    suspendSetValue = { DataStoreService.suspendSetValue(serializer, cacheKey, it) }
//)
//
//inline fun <reified T> DataStoreProperty.Companion.initSerializableOpt(
//    serializer: KSerializer<T>,
//    cacheKey: String,
//    initPath: T?
//) = DataStoreProperty(
//    flow = DataStoreService.getSerializableOptFlow(serializer, cacheKey, initPath),
//    getValue = { DataStoreService.getOptValue(serializer, cacheKey, initPath) },
//    setValue = { DataStoreService.setOptValue(serializer, cacheKey, it) },
//    suspendSetValue = { DataStoreService.suspendSetOptValue(serializer, cacheKey, it) }
//)
////endregion