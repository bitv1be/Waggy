package ru.bitvibe.waggy.domain.models

data class BreedRecommendation(
    val breed: Breed,
    val reason: RecommendationReason,
)

data class RecommendationResult(
    val recommendations: List<BreedRecommendation>,
    val source: RecommendationSource,
)

enum class RecommendationSource {
    PERSONALIZED,
    POPULAR,
}

sealed interface RecommendationReason {
    data class AiGenerated(
        val explanation: String,
    ) : RecommendationReason
}
