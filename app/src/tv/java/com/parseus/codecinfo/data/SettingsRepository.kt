package com.parseus.codecinfo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceDataStore
import okio.Path.Companion.toOkioPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

private val dataStoreLock = Any()
private var dataStoreInstance: DataStore<Preferences>? = null
val Context.dataStore: DataStore<Preferences>
    get() = dataStoreInstance ?: synchronized(dataStoreLock) {
        dataStoreInstance ?: PreferenceDataStoreFactory.createWithPath(
            produceFile = { applicationContext.filesDir.resolve("datastore/settings.preferences_pb").toOkioPath() },
            migrations = listOf(
                SharedPreferencesMigration(applicationContext, "${applicationContext.packageName}_preferences")
            )
        ).also { dataStoreInstance = it }
    }

private val repositoryLock = Any()
private var repositoryInstance: SettingsRepository? = null
val Context.settingsRepository: SettingsRepository
    get() {
        return repositoryInstance ?: synchronized(repositoryLock) {
            repositoryInstance ?: SettingsRepository(applicationContext).also { repositoryInstance = it }
        }
    }

data class Settings(
    val knownValuesColorProfiles: String,
    val knownValuesProfileLevels: String,
    val knownResolutions: String,
    val showHwIcon: Boolean,
    val saveDetailsToLogcat: Boolean,
    val filterType: String,
    val sortType: String,
    val showAliases: Boolean,
    val showHwCodecsOnly: Boolean
)

class SettingsRepository(context: Context) : PreferenceDataStore() {

    private val dataStore = context.dataStore
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val preferencesStateFlow: StateFlow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking {
                try { dataStore.data.first() } catch (_: Exception) { emptyPreferences() }
            }
        )

    val settingsStateFlow: StateFlow<Settings> = preferencesStateFlow
        .map { mapSettings(it) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = mapSettings(preferencesStateFlow.value)
        )
    val settingsFlow: Flow<Settings> = settingsStateFlow

    fun getSettingsSync(): Settings = settingsStateFlow.value

    private fun mapSettings(preferences: Preferences): Settings {
        return Settings(
            knownValuesColorProfiles = preferences[KNOWN_VALUES_COLOR_PROFILES] ?: "1",
            knownValuesProfileLevels = preferences[KNOWN_VALUES_PROFILE_LEVELS] ?: "1",
            knownResolutions = preferences[KNOWN_RESOLUTIONS] ?: "0",
            showHwIcon = preferences[SHOW_HW_ICON] ?: true,
            saveDetailsToLogcat = preferences[SAVE_DETAILS_TO_LOGCAT] ?: false,
            filterType = preferences[FILTER_TYPE] ?: "2",
            sortType = preferences[SORT_TYPE] ?: "0",
            showAliases = preferences[SHOW_ALIASES] ?: false,
            showHwCodecsOnly = preferences[SHOW_HW_CODECS_ONLY] ?: false
        )
    }

    // PreferenceDataStore implementation
    override fun putString(key: String, value: String?) {
        val prefKey = stringPreferencesKey(key)
        val newValue = value ?: ""
        if (preferencesStateFlow.value[prefKey] == newValue) return
        scope.launch { dataStore.edit { it[prefKey] = newValue } }
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferencesStateFlow.value[stringPreferencesKey(key)] ?: defValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        if (preferencesStateFlow.value[prefKey] == value) return
        scope.launch { dataStore.edit { it[prefKey] = value } }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferencesStateFlow.value[booleanPreferencesKey(key)] ?: defValue
    }

    override fun putInt(key: String, value: Int) {
        val prefKey = intPreferencesKey(key)
        if (preferencesStateFlow.value[prefKey] == value) return
        scope.launch { dataStore.edit { it[prefKey] = value } }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferencesStateFlow.value[intPreferencesKey(key)] ?: defValue
    }

    override fun putLong(key: String, value: Long) {
        val prefKey = longPreferencesKey(key)
        if (preferencesStateFlow.value[prefKey] == value) return
        scope.launch { dataStore.edit { it[prefKey] = value } }
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferencesStateFlow.value[longPreferencesKey(key)] ?: defValue
    }

    companion object {
        val KNOWN_VALUES_COLOR_PROFILES = stringPreferencesKey("known_values_color_profiles")
        val KNOWN_VALUES_PROFILE_LEVELS = stringPreferencesKey("known_values_profile_levels")
        val KNOWN_RESOLUTIONS = stringPreferencesKey("known_resolutions")
        val SHOW_HW_ICON = booleanPreferencesKey("show_hw_icon")
        val SAVE_DETAILS_TO_LOGCAT = booleanPreferencesKey("save_details_to_logcat")
        val FILTER_TYPE = stringPreferencesKey("filter_type")
        val SORT_TYPE = stringPreferencesKey("sort_type")
        val SHOW_ALIASES = booleanPreferencesKey("show_aliases")
        val SHOW_HW_CODECS_ONLY = booleanPreferencesKey("show_hw_codecs_only")
    }
}