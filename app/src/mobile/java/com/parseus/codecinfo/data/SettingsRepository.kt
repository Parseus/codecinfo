package com.parseus.codecinfo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceDataStore
import com.parseus.codecinfo.ui.settings.DarkTheme
import com.parseus.codecinfo.utils.getDefaultThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(context, "${context.packageName}_preferences"),
            SharedPreferencesMigration(context, "rate_bottom_sheet_pref")
        )
    }
)

val Context.settingsRepository: SettingsRepository
    get() = SettingsRepository(this)

data class Settings(
    val darkTheme: Int,
    val dynamicTheme: Boolean,
    val dynamicThemeWallpaperSource: String,
    val immersiveMode: Boolean,
    val knownValuesColorProfiles: String,
    val knownValuesProfileLevels: String,
    val knownResolutions: String,
    val showHwIcon: Boolean,
    val saveDetailsToLogcat: Boolean,
    val openExternalLinks: String,
    val filterType: String,
    val sortType: String,
    val showAliases: Boolean,
    val showHwCodecsOnly: Boolean,
    val selectedColor: Int?
)

class SettingsRepository(context: Context) : PreferenceDataStore() {

    private val dataStore = context.dataStore

    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            darkTheme = (preferences[DARK_THEME] ?: DarkTheme.fromValue(getDefaultThemeOption(context)).toString()).toInt(),
            dynamicTheme = preferences[DYNAMIC_THEME] ?: false,
            dynamicThemeWallpaperSource = preferences[DYNAMIC_THEME_WALLPAPER_SOURCE] ?: "1",
            immersiveMode = preferences[IMMERSIVE_MODE] ?: true,
            knownValuesColorProfiles = preferences[KNOWN_VALUES_COLOR_PROFILES] ?: "1",
            knownValuesProfileLevels = preferences[KNOWN_VALUES_PROFILE_LEVELS] ?: "1",
            knownResolutions = preferences[KNOWN_RESOLUTIONS] ?: "0",
            showHwIcon = preferences[SHOW_HW_ICON] ?: true,
            saveDetailsToLogcat = preferences[SAVE_DETAILS_TO_LOGCAT] ?: false,
            openExternalLinks = preferences[OPEN_EXTERNAL_LINKS] ?: "0",
            filterType = preferences[FILTER_TYPE] ?: "2",
            sortType = preferences[SORT_TYPE] ?: "0",
            showAliases = preferences[SHOW_ALIASES] ?: false,
            showHwCodecsOnly = preferences[SHOW_HW_CODECS_ONLY] ?: false,
            selectedColor = preferences[SELECTED_COLOR]
        )
    }

    fun getSettingsSync(): Settings = runBlocking {
        settingsFlow.first()
    }

    // Rate Bottom Sheet Preferences
    val installDays: Flow<Long> = dataStore.data.map { it[PREF_INSTALL_DAYS] ?: 0L }
    val cptLaunchTimes: Flow<Int> = dataStore.data.map { it[PREF_CPT_LAUNCH_TIMES] ?: 0 }
    val isAgreeShowBottomSheet: Flow<Boolean> = dataStore.data.map { it[PREF_IS_AGREE_SHOW_BOTTOM_SHEET] ?: true }
    val remindInterval: Flow<Long> = dataStore.data.map { it[PREF_REMIND_INTERVAL] ?: 0L }

    suspend fun setInstallDays(days: Long) {
        dataStore.edit { it[PREF_INSTALL_DAYS] = days }
    }

    suspend fun setCptLaunchTimes(times: Int) {
        dataStore.edit { it[PREF_CPT_LAUNCH_TIMES] = times }
    }

    suspend fun setAgreeShowBottomSheet(agree: Boolean) {
        dataStore.edit { it[PREF_IS_AGREE_SHOW_BOTTOM_SHEET] = agree }
    }

    suspend fun setRemindInterval(interval: Long) {
        dataStore.edit { it[PREF_REMIND_INTERVAL] = interval }
    }

    suspend fun clearRatePrefs() {
        dataStore.edit {
            it.remove(PREF_INSTALL_DAYS)
            it.remove(PREF_CPT_LAUNCH_TIMES)
            it.remove(PREF_IS_AGREE_SHOW_BOTTOM_SHEET)
            it.remove(PREF_REMIND_INTERVAL)
        }
    }

    suspend fun setSelectedColor(color: Int) {
        dataStore.edit { it[SELECTED_COLOR] = color }
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
        val DARK_THEME = stringPreferencesKey("dark_theme")
        val DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
        val DYNAMIC_THEME_WALLPAPER_SOURCE = stringPreferencesKey("dynamic_theme_wallpaper_source")
        val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
        val KNOWN_VALUES_COLOR_PROFILES = stringPreferencesKey("known_values_color_profiles")
        val KNOWN_VALUES_PROFILE_LEVELS = stringPreferencesKey("known_values_profile_levels")
        val KNOWN_RESOLUTIONS = stringPreferencesKey("known_resolutions")
        val SHOW_HW_ICON = booleanPreferencesKey("show_hw_icon")
        val SAVE_DETAILS_TO_LOGCAT = booleanPreferencesKey("save_details_to_logcat")
        val OPEN_EXTERNAL_LINKS = stringPreferencesKey("open_external_links")
        val FILTER_TYPE = stringPreferencesKey("filter_type")
        val SORT_TYPE = stringPreferencesKey("sort_type")
        val SHOW_ALIASES = booleanPreferencesKey("show_aliases")
        val SHOW_HW_CODECS_ONLY = booleanPreferencesKey("show_hw_codecs_only")
        val SELECTED_COLOR = intPreferencesKey("selected_color")

        private val PREF_INSTALL_DAYS = longPreferencesKey("pref_rate_install_days")
        private val PREF_CPT_LAUNCH_TIMES = intPreferencesKey("pref_rate_cpt_launch_times")
        private val PREF_IS_AGREE_SHOW_BOTTOM_SHEET = booleanPreferencesKey("pref_rate_is_agree_show_bottom_sheet")
        private val PREF_REMIND_INTERVAL = longPreferencesKey("pref_rate_remind_interval")
    }
}