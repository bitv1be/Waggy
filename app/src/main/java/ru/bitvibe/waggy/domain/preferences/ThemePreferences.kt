package ru.bitvibe.waggy.domain.preferences

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

interface ThemePreferences {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}