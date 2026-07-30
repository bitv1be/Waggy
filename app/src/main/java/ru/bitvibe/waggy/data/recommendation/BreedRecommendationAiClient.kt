package ru.bitvibe.waggy.data.recommendation

data class AiRecommendationInput(
    val availableBreedNames: List<String>,
    val favoriteBreedNames: List<String>,
    val limit: Int,
    val languageTag: String,
    val strictLanguage: Boolean = false,
)

data class AiBreedRecommendation(
    val breedName: String,
    val explanation: String,
)

interface BreedRecommendationAiClient {
    suspend fun generateRecommendations(
        input: AiRecommendationInput,
    ): List<AiBreedRecommendation>
}
