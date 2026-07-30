package ru.bitvibe.waggy.presentation.breeds

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.repository.RecommendationRequest
import ru.bitvibe.waggy.domain.usecase.GetAllBreedsUseCase
import ru.bitvibe.waggy.domain.usecase.ObserveAllFavoritesUseCase
import javax.inject.Inject

@HiltViewModel
class BreedsViewModel @Inject constructor(
    private val getAllBreedsUseCase: GetAllBreedsUseCase,
    private val observeAllFavoritesUseCase: ObserveAllFavoritesUseCase,
    private val recommendationStateLoader: RecommendationStateLoader,
) : ViewModel() {
    private companion object {
        const val TAG = "BreedsViewModel"
    }

    private val _state = MutableStateFlow(BreedsUiState())
    val state = _state.asStateFlow()
    private val observedFavorites = MutableStateFlow<List<Favorite>?>(null)
    private var recommendationJob: Job? = null

    init {
        observeFavorites()
        loadBreeds()
    }

    fun onEvent(event: BreedsEvent) {
        when (event) {
            is BreedsEvent.OnRefresh -> loadBreeds(event.forced)
            BreedsEvent.OnRetryRecommendations -> {
                val favorites = observedFavorites.value ?: return
                loadRecommendations(
                    breeds = _state.value.breeds,
                    favorites = favorites,
                    forceRefresh = true,
                )
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                observeAllFavoritesUseCase()
                    .distinctUntilChanged()
                    .collect { newFavorites ->
                        observedFavorites.value = newFavorites
                        val breeds = _state.value.breeds
                        if (breeds.isNotEmpty()) {
                            loadRecommendations(
                                breeds = breeds,
                                favorites = newFavorites,
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = recordException(e)
                _state.update {
                    it.copy(recommendations = RecommendationsUiState.Error(message))
                }
            }
        }
    }

    private fun loadBreeds(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            try {
                val result = getAllBreedsUseCase(force)
                _state.update {
                    it.copy(
                        isLoading = false,
                        breeds = result,
                    )
                }
                observedFavorites.value?.let { favorites ->
                    loadRecommendations(
                        breeds = result,
                        favorites = favorites,
                        forceRefresh = force,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = recordException(e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = message,
                    )
                }
            }
        }
    }

    private fun loadRecommendations(
        breeds: List<Breed>,
        favorites: List<Favorite>,
        forceRefresh: Boolean = false,
    ) {
        recommendationJob?.cancel()
        if (breeds.isEmpty()) {
            _state.update { it.copy(recommendations = RecommendationsUiState.Empty) }
            return
        }
        recommendationJob = viewModelScope.launch(Dispatchers.IO) {
            val outcome = recommendationStateLoader(
                request = RecommendationRequest(
                    availableBreeds = breeds,
                    favorites = favorites,
                    forceRefresh = forceRefresh,
                ),
                onLoading = { loadingState ->
                    _state.update { it.copy(recommendations = loadingState) }
                },
            )
            _state.update { it.copy(recommendations = outcome.state) }
            outcome.error?.let(::recordException)
        }
    }

    private fun recordException(error: Exception): String {
        val message = error.message ?: "Unknown error"
        Firebase.crashlytics.log(message)
        Firebase.crashlytics.recordException(error)
        Log.e(TAG, message, error)
        return message
    }
}
