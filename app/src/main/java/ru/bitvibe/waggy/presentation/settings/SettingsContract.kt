package ru.bitvibe.waggy.presentation.settings

import ru.bitvibe.waggy.domain.models.AppUpdate
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.preferences.ThemeMode

data class SettingsUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val appUpdateState: AppUpdateUiState = AppUpdateUiState.Idle,
)

sealed interface SettingsEvent {
    data object OnRefresh : SettingsEvent
    data class OnRemove(val favorite: Favorite) : SettingsEvent
    data object OnClearAll : SettingsEvent
    data class OnSetTheme(val mode: ThemeMode) : SettingsEvent
    data class OnSetWidgetPeriod(val minutes: Long) : SettingsEvent
    data object OnCheckForUpdate : SettingsEvent
    data object OnDownloadUpdate : SettingsEvent
    data object OnInstallerLaunched : SettingsEvent
    data object OnInstallPermissionDenied : SettingsEvent
    data object OnInstallLaunchFailed : SettingsEvent
}

sealed interface AppUpdateUiState {
    data object Idle : AppUpdateUiState
    data object Checking : AppUpdateUiState
    data object UpToDate : AppUpdateUiState
    data class Available(val update: AppUpdate) : AppUpdateUiState
    data class Downloading(
        val update: AppUpdate,
        val progressPercent: Int?,
    ) : AppUpdateUiState

    data class ReadyToInstall(
        val update: AppUpdate,
        val apkUri: String,
    ) : AppUpdateUiState

    data class Downloaded(
        val update: AppUpdate,
        val apkUri: String,
        val permissionDenied: Boolean = false,
        val installLaunchFailed: Boolean = false,
    ) : AppUpdateUiState

    data class Error(val operation: AppUpdateOperation) : AppUpdateUiState
}

enum class AppUpdateOperation {
    CHECK,
    DOWNLOAD,
}
