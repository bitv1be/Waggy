package ru.bitvibe.waggy.domain.repository

import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.models.RecommendationResult

data class RecommendationRequest(
    val availableBreeds: List<Breed>,
    val favorites: List<Favorite>,
    val limit: Int = DEFAULT_RECOMMENDATION_LIMIT,
    val forceRefresh: Boolean = false,
) {
    init {
        require(limit > 0) { "Recommendation limit must be positive." }
    }

    private companion object {
        const val DEFAULT_RECOMMENDATION_LIMIT = 6
    }
}

interface RecommendationProvider {
    suspend fun getRecommendations(request: RecommendationRequest): RecommendationResult
}
