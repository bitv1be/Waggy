package ru.bitvibe.waggy.presentation.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.preferences.ThemePreferences
import ru.bitvibe.waggy.domain.usecase.ClearAllFavoritesUseCase
import ru.bitvibe.waggy.domain.usecase.GetAllFavoritesUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedFavoriteUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedParams
import ru.bitvibe.waggy.domain.usecase.UseCase
import javax.inject.Inject
import android.content.ComponentName
import android.appwidget.AppWidgetManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.bitvibe.waggy.domain.preferences.WidgetPreferences
import ru.bitvibe.waggy.presentation.widget.BreedWidgetReceiver
import ru.bitvibe.waggy.presentation.widget.BreedWidgetWorker

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAllFavoritesUseCase: GetAllFavoritesUseCase,
    private val toggleBreedFavoriteUseCase: ToggleBreedFavoriteUseCase,
    private val clearAllFavoritesUseCase: ClearAllFavoritesUseCase,
    private val themePreferences: ThemePreferences,
    private val widgetPreferences: WidgetPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private companion object {
        const val TAG = "SettingsViewModel"
    }

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    val isDarkMode: StateFlow<Boolean?> = themePreferences.isDarkMode
    val widgetPeriodMinutes: StateFlow<Long> = widgetPreferences.updatePeriodMinutes

    init {
        loadFavorites()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnRefresh -> loadFavorites()
            is SettingsEvent.OnRemove -> removeFavorite(event.favorite)
            is SettingsEvent.OnClearAll -> clearAllFavorites()
            is SettingsEvent.OnSetTheme -> setThemeMode(event.isDark)
            is SettingsEvent.OnSetWidgetPeriod -> setWidgetPeriod(event.minutes)
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val favorites = getAllFavoritesUseCase(UseCase.None)
                _state.update { it.copy(favorites = favorites, isLoading = false) }
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
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

    private fun setThemeMode(isDark: Boolean?) {
        viewModelScope.launch(Dispatchers.IO) {
            themePreferences.setDarkMode(isDark)
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
