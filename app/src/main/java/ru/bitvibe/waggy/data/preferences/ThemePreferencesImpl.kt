package ru.bitvibe.waggy.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.bitvibe.waggy.domain.preferences.ThemeMode
import ru.bitvibe.waggy.domain.preferences.ThemePreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferencesImpl @Inject constructor(
    @ThemeDataStore private val dataStore: DataStore<Preferences>
) : ThemePreferences {
    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    override val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs ->
            prefs[KEY_THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
        }
        .distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }
}
