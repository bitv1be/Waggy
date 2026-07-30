package ru.bitvibe.waggy.presentation.breeds

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.BreedRecommendation
import ru.bitvibe.waggy.domain.models.RecommendationReason
import ru.bitvibe.waggy.domain.models.RecommendationResult
import ru.bitvibe.waggy.domain.models.RecommendationSource
import ru.bitvibe.waggy.domain.repository.RecommendationProvider
import ru.bitvibe.waggy.domain.repository.RecommendationRequest

class RecommendationStateLoaderTest {
    @Test
    fun load_emitsLoadingBeforeContent() = runBlocking {
        var loadingWasEmitted = false
        val recommendation = BreedRecommendation(
            breed = Breed("labrador", null, emptyList()),
            reason = RecommendationReason.AiGenerated("A good match."),
        )
        val loader = RecommendationStateLoader(
            FakeRecommendationProvider {
                assertTrue(loadingWasEmitted)
                RecommendationResult(
                    recommendations = listOf(recommendation),
                    source = RecommendationSource.POPULAR,
                )
            },
        )

        val outcome = loader(emptyRequest()) { state ->
            loadingWasEmitted = state == RecommendationsUiState.Loading
        }

        assertTrue(loadingWasEmitted)
        val content = outcome.state as RecommendationsUiState.Content
        assertEquals(listOf(recommendation), content.result.recommendations)
    }

    @Test
    fun load_mapsEmptyProviderResultToEmptyState() = runBlocking {
        val loader = RecommendationStateLoader(
            FakeRecommendationProvider {
                RecommendationResult(
                    recommendations = emptyList(),
                    source = RecommendationSource.PERSONALIZED,
                )
            },
        )

        val outcome = loader(emptyRequest()) {}

        assertEquals(RecommendationsUiState.Empty, outcome.state)
    }

    @Test
    fun load_mapsProviderFailureToRetryableErrorState() = runBlocking {
        val expectedError = IllegalStateException("AI unavailable")
        var loadingWasEmitted = false
        val loader = RecommendationStateLoader(
            FakeRecommendationProvider { throw expectedError },
        )

        val outcome = loader(emptyRequest()) { state ->
            loadingWasEmitted = state == RecommendationsUiState.Loading
        }

        assertTrue(loadingWasEmitted)
        assertEquals(
            RecommendationsUiState.Error("AI unavailable"),
            outcome.state,
        )
        assertSame(expectedError, outcome.error)
    }

    private fun emptyRequest(): RecommendationRequest {
        return RecommendationRequest(
            availableBreeds = emptyList(),
            favorites = emptyList(),
        )
    }
}

private class FakeRecommendationProvider(
    private val result: suspend () -> RecommendationResult,
) : RecommendationProvider {
    override suspend fun getRecommendations(request: RecommendationRequest): RecommendationResult {
        return result()
    }
}
