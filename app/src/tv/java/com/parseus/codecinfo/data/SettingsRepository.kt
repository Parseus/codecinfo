package com.parseus.codecinfo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(context, "${context.packageName}_preferences")
        )
    }
)

val Context.settingsRepository: SettingsRepository
    get() = SettingsRepository(this)

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

    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
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

    fun getSettingsSync(): Settings = runBlocking {
        settingsFlow.first()
    }

    // PreferenceDataStore implementation
    override fun putString(key: String, value: String?) {
        runBlocking {
            dataStore.edit { it[stringPreferencesKey(key)] = value ?: "" }
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return runBlocking {
            dataStore.data.map { it[stringPreferencesKey(key)] }.first() ?: defValue
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { it[booleanPreferencesKey(key)] = value }
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return runBlocking {
            dataStore.data.map { it[booleanPreferencesKey(key)] }.first() ?: defValue
        }
    }

    override fun putInt(key: String, value: Int) {
        runBlocking {
            dataStore.edit { it[intPreferencesKey(key)] = value }
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return runBlocking {
            dataStore.data.map { it[intPreferencesKey(key)] }.first() ?: defValue
        }
    }

    override fun putLong(key: String, value: Long) {
        runBlocking {
            dataStore.edit { it[longPreferencesKey(key)] = value }
        }
    }

    override fun getLong(key: String, defValue: Long): Long {
        return runBlocking {
            dataStore.data.map { it[longPreferencesKey(key)] }.first() ?: defValue
        }
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