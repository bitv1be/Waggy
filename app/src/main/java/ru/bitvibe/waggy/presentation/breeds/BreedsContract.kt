package ru.bitvibe.waggy.presentation.breeds

import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.RecommendationResult

data class BreedsUiState(
    val isLoading: Boolean = false,
    val breeds: List<Breed> = emptyList(),
    val recommendations: RecommendationsUiState = RecommendationsUiState.Loading,
    val error: String? = null,
)

sealed interface RecommendationsUiState {
    data object Loading : RecommendationsUiState

    data class Content(
        val result: RecommendationResult,
    ) : RecommendationsUiState

    data object Empty : RecommendationsUiState

    data class Error(
        val message: String,
    ) : RecommendationsUiState
}

sealed interface BreedsEvent {
    data class OnRefresh(val forced: Boolean = false) : BreedsEvent
    data object OnRetryRecommendations : BreedsEvent
}
