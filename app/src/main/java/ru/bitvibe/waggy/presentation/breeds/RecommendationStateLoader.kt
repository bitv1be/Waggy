package ru.bitvibe.waggy.presentation.breeds

import kotlinx.coroutines.CancellationException
import ru.bitvibe.waggy.domain.models.RecommendationResult
import ru.bitvibe.waggy.domain.repository.RecommendationProvider
import ru.bitvibe.waggy.domain.repository.RecommendationRequest
import javax.inject.Inject

data class RecommendationLoadOutcome(
    val state: RecommendationsUiState,
    val error: Exception? = null,
)

class RecommendationStateLoader @Inject constructor(
    private val provider: RecommendationProvider,
) {
    suspend operator fun invoke(
        request: RecommendationRequest,
        onLoading: (RecommendationsUiState) -> Unit,
    ): RecommendationLoadOutcome {
        onLoading(RecommendationsUiState.Loading)
        return try {
            RecommendationLoadOutcome(provider.getRecommendations(request).toUiState())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            RecommendationLoadOutcome(
                state = RecommendationsUiState.Error(error.message ?: "Unknown error"),
                error = error,
            )
        }
    }

    private fun RecommendationResult.toUiState(): RecommendationsUiState {
        return if (recommendations.isEmpty()) {
            RecommendationsUiState.Empty
        } else {
            RecommendationsUiState.Content(this)
        }
    }
}
