package ru.bitvibe.waggy.data.recommendation

interface AiResponseLanguageValidator {
    suspend fun matches(
        text: String,
        expectedLanguageTag: String,
    ): Boolean
}
