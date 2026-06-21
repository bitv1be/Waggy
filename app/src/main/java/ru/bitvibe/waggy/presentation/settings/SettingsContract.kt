package ru.bitvibe.waggy.presentation.settings

import ru.bitvibe.waggy.domain.models.Favorite

data class SettingsUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface SettingsEvent {
    data object OnRefresh : SettingsEvent
    data class OnRemove(val favorite: Favorite) : SettingsEvent
    data object OnClearAll : SettingsEvent
    data class OnSetTheme(val isDark: Boolean?) : SettingsEvent
    data class OnSetWidgetPeriod(val minutes: Long) : SettingsEvent
}