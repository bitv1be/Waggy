package ru.bitvibe.waggy.domain.preferences

import kotlinx.coroutines.flow.StateFlow

interface ThemePreferences {
    val isDarkMode: StateFlow<Boolean?>
    fun setDarkMode(isDark: Boolean?)
}
