package com.parseus.codecinfo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceDataStore
import androidx.tracing.trace
import com.parseus.codecinfo.ui.settings.DarkTheme
import com.parseus.codecinfo.utils.getDefaultThemeOption
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
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.IOException

private val dataStoreLock = Any()
private var dataStoreInstance: DataStore<Preferences>? = null
val Context.dataStore: DataStore<Preferences>
    get() = dataStoreInstance ?: synchronized(dataStoreLock) {
        dataStoreInstance ?: PreferenceDataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = { applicationContext.filesDir.resolve("datastore/settings.preferences_pb").toOkioPath() }
            ),
            migrations = listOf(
                SharedPreferencesMigration(applicationContext, "${applicationContext.packageName}_preferences"),
                SharedPreferencesMigration(applicationContext, "rate_bottom_sheet_pref")
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

class SettingsRepository(private val context: Context) : PreferenceDataStore() {

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

    private fun mapSettings(preferences: Preferences): Settings = trace("Settings.map") {
        val defaultThemeValue = getDefaultThemeOption(context)
        val darkTheme = preferences[DARK_THEME]?.toIntOrNull() ?: defaultThemeValue
        val theme = DarkTheme.fromValue(darkTheme) ?: DarkTheme.fromValue(defaultThemeValue)!!

        return Settings(
            darkTheme = theme.value,
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

    // Rate Bottom Sheet Preferences
    val installDays: Flow<Long> = preferencesStateFlow.map { it[PREF_INSTALL_DAYS] ?: 0L }
    val cptLaunchTimes: Flow<Int> = preferencesStateFlow.map { it[PREF_CPT_LAUNCH_TIMES] ?: 0 }
    val isAgreeShowBottomSheet: Flow<Boolean> = preferencesStateFlow.map { it[PREF_IS_AGREE_SHOW_BOTTOM_SHEET] ?: true }
    val remindInterval: Flow<Long> = preferencesStateFlow.map { it[PREF_REMIND_INTERVAL] ?: 0L }

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
        val prefKey = stringPreferencesKey(key)
        val newValue = value ?: ""
        if (preferencesStateFlow.value[prefKey] == newValue) return
        scope.launch { dataStore.edit { it[prefKey] = newValue } }
    }

    override fun getString(key: String, defValue: String?): String? {
        val value = preferencesStateFlow.value[stringPreferencesKey(key)]
        if (value != null) return value
        if (defValue != null) return defValue

        return when (key) {
            DARK_THEME.name -> getDefaultThemeOption(context).toString()
            DYNAMIC_THEME_WALLPAPER_SOURCE.name -> "1"
            KNOWN_VALUES_COLOR_PROFILES.name -> "1"
            KNOWN_VALUES_PROFILE_LEVELS.name -> "1"
            KNOWN_RESOLUTIONS.name -> "0"
            OPEN_EXTERNAL_LINKS.name -> "0"
            FILTER_TYPE.name -> "2"
            SORT_TYPE.name -> "0"
            else -> null
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        if (preferencesStateFlow.value[prefKey] == value) return
        scope.launch { dataStore.edit { it[prefKey] = value } }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val value = preferencesStateFlow.value[booleanPreferencesKey(key)]
        if (value != null) return value

        return when (key) {
            DYNAMIC_THEME.name -> false
            IMMERSIVE_MODE.name -> true
            SHOW_HW_ICON.name -> true
            SAVE_DETAILS_TO_LOGCAT.name -> false
            SHOW_ALIASES.name -> false
            SHOW_HW_CODECS_ONLY.name -> false
            else -> defValue
        }
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