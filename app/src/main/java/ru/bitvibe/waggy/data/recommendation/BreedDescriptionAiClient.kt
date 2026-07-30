package ru.bitvibe.waggy.data.recommendation

data class AiBreedDescriptionInput(
    val breedName: String,
    val subBreedNames: List<String>,
    val languageTag: String,
    val strictLanguage: Boolean = false,
)

interface BreedDescriptionAiClient {
    suspend fun generateDescription(input: AiBreedDescriptionInput): String
}
