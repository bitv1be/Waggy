package ru.bitvibe.waggy.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.bitvibe.waggy.domain.preferences.ThemePreferences

class ThemePreferencesImpl(
    context: Context
) : ThemePreferences {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    override val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    override fun setDarkMode(isDark: Boolean?) {
        if (isDark == null) {
            prefs.edit { remove("is_dark_mode") }
        } else {
            prefs.edit { putBoolean("is_dark_mode", isDark) }
        }
        _isDarkMode.value = isDark
    }
}
