package ru.bitvibe.waggy.presentation.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.bitvibe.waggy.BuildConfig
import ru.bitvibe.waggy.data.update.AppUpdateDownloader
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.preferences.ThemeMode
import ru.bitvibe.waggy.domain.preferences.ThemePreferences
import ru.bitvibe.waggy.domain.preferences.WidgetPreferences
import ru.bitvibe.waggy.domain.usecase.CheckForAppUpdateUseCase
import ru.bitvibe.waggy.domain.usecase.ClearAllFavoritesUseCase
import ru.bitvibe.waggy.domain.usecase.GetAllFavoritesUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedFavoriteUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedParams
import ru.bitvibe.waggy.domain.usecase.UseCase
import ru.bitvibe.waggy.presentation.widget.BreedWidgetReceiver
import ru.bitvibe.waggy.presentation.widget.BreedWidgetWorker
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAllFavoritesUseCase: GetAllFavoritesUseCase,
    private val toggleBreedFavoriteUseCase: ToggleBreedFavoriteUseCase,
    private val clearAllFavoritesUseCase: ClearAllFavoritesUseCase,
    private val checkForAppUpdateUseCase: CheckForAppUpdateUseCase,
    private val appUpdateDownloader: AppUpdateDownloader,
    private val themePreferences: ThemePreferences,
    private val widgetPreferences: WidgetPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private companion object {
        const val TAG = "SettingsViewModel"
    }

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    val isDarkMode: Flow<ThemeMode> = themePreferences.themeMode
    val widgetPeriodMinutes: StateFlow<Long> = widgetPreferences.updatePeriodMinutes

    init {
        loadFavorites()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnRefresh -> loadFavorites()
            is SettingsEvent.OnRemove -> removeFavorite(event.favorite)
            is SettingsEvent.OnClearAll -> clearAllFavorites()
            is SettingsEvent.OnSetTheme -> setThemeMode(event.mode)
            is SettingsEvent.OnSetWidgetPeriod -> setWidgetPeriod(event.minutes)
            SettingsEvent.OnCheckForUpdate -> checkForUpdate()
            SettingsEvent.OnDownloadUpdate -> downloadUpdate()
            SettingsEvent.OnInstallerLaunched -> markUpdateDownloaded()
            SettingsEvent.OnInstallPermissionDenied -> markInstallPermissionDenied()
            SettingsEvent.OnInstallLaunchFailed -> markInstallLaunchFailed()
        }
    }

    private fun checkForUpdate() {
        if (_state.value.appUpdateState is AppUpdateUiState.Checking ||
            _state.value.appUpdateState is AppUpdateUiState.Downloading
        ) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(appUpdateState = AppUpdateUiState.Checking) }
            try {
                val update = checkForAppUpdateUseCase(BuildConfig.VERSION_NAME)
                _state.update {
                    it.copy(
                        appUpdateState = update?.let(AppUpdateUiState::Available)
                            ?: AppUpdateUiState.UpToDate,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                recordException(e)
                _state.update {
                    it.copy(
                        appUpdateState = AppUpdateUiState.Error(AppUpdateOperation.CHECK),
                    )
                }
            }
        }
    }

    private fun downloadUpdate() {
        val update = (_state.value.appUpdateState as? AppUpdateUiState.Available)?.update
            ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(appUpdateState = AppUpdateUiState.Downloading(update, null))
            }
            try {
                val apkUri = appUpdateDownloader.download(update) { progressPercent ->
                    _state.update { state ->
                        if (state.appUpdateState is AppUpdateUiState.Downloading) {
                            state.copy(
                                appUpdateState = AppUpdateUiState.Downloading(
                                    update = update,
                                    progressPercent = progressPercent,
                                ),
                            )
                        } else {
                            state
                        }
                    }
                }
                _state.update {
                    it.copy(
                        appUpdateState = AppUpdateUiState.ReadyToInstall(
                            update = update,
                            apkUri = apkUri.toString(),
                        ),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                recordException(e)
                _state.update {
                    it.copy(
                        appUpdateState = AppUpdateUiState.Error(AppUpdateOperation.DOWNLOAD),
                    )
                }
            }
        }
    }

    private fun markUpdateDownloaded() {
        _state.update { state ->
            val updateState = state.appUpdateState as? AppUpdateUiState.ReadyToInstall
                ?: return@update state
            state.copy(
                appUpdateState = AppUpdateUiState.Downloaded(
                    update = updateState.update,
                    apkUri = updateState.apkUri,
                ),
            )
        }
    }

    private fun markInstallPermissionDenied() {
        _state.update { state ->
            val downloaded = when (val updateState = state.appUpdateState) {
                is AppUpdateUiState.ReadyToInstall -> AppUpdateUiState.Downloaded(
                    update = updateState.update,
                    apkUri = updateState.apkUri,
                    permissionDenied = true,
                )

                is AppUpdateUiState.Downloaded -> updateState.copy(
                    permissionDenied = true,
                )

                else -> return@update state
            }
            state.copy(appUpdateState = downloaded)
        }
    }

    private fun markInstallLaunchFailed() {
        _state.update { state ->
            val downloaded = when (val updateState = state.appUpdateState) {
                is AppUpdateUiState.ReadyToInstall -> AppUpdateUiState.Downloaded(
                    update = updateState.update,
                    apkUri = updateState.apkUri,
                    installLaunchFailed = true,
                )

                is AppUpdateUiState.Downloaded -> updateState.copy(
                    installLaunchFailed = true,
                )

                else -> return@update state
            }
            state.copy(appUpdateState = downloaded)
        }
    }

    private fun recordException(exception: Exception) {
        val message = exception.message ?: "Unknown error"
        Firebase.crashlytics.log(message)
        Firebase.crashlytics.recordException(exception)
        Log.e(TAG, message, exception)
    }

    private fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val favorites = getAllFavoritesUseCase(UseCase.None)
                _state.update { it.copy(favorites = favorites, isLoading = false) }
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                Firebase.crashlytics.log(message)
                Firebase.crashlytics.recordException(e)
                Log.e(TAG, message)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = message
                    )
                }
            }
        }
    }

    private fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toggleBreedFavoriteUseCase(
                    ToggleBreedParams(
                        name = favorite.breedName,
                        subName = favorite.subBreedName,
                        isFavorite = false
                    )
                )
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                Firebase.crashlytics.log(message)
                Firebase.crashlytics.recordException(e)
                Log.e(TAG, message)
                _state.update {
                    it.copy(
                        error = message
                    )
                }
            }
        }

        loadFavorites()
    }

    private fun clearAllFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                clearAllFavoritesUseCase(UseCase.None)
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                Firebase.crashlytics.log(message)
                Firebase.crashlytics.recordException(e)
                Log.e(TAG, message)
                _state.update {
                    it.copy(
                        error = message
                    )
                }
            }
        }

        loadFavorites()
    }

    private fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            themePreferences.setThemeMode(mode)
        }
    }

    private fun setWidgetPeriod(minutes: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            widgetPreferences.setUpdatePeriodMinutes(minutes)

            // Re-enqueue work for all widgets with the new interval
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, BreedWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            appWidgetIds.forEach { appWidgetId ->
                BreedWidgetWorker.enqueuePeriodicWork(context, appWidgetId, force = true)
            }
        }
    }
}
