package ru.bitvibe.waggy.data.recommendation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAiBreedRecommendationClientTest {
    private val client = FirebaseAiBreedRecommendationClient()

    @Test
    fun parseResponse_readsStructuredRecommendationJson() {
        val recommendations = client.parseResponse(
            """
                {
                  "recommendations": [
                    {
                      "breedName": "poodle",
                      "explanation": "Adaptable and highly trainable."
                    }
                  ]
                }
            """.trimIndent(),
        )

        assertEquals(
            listOf(
                AiBreedRecommendation(
                    breedName = "poodle",
                    explanation = "Adaptable and highly trainable.",
                ),
            ),
            recommendations,
        )
    }

    @Test
    fun buildPrompt_usesExplicitLanguageAndCorrectiveRetry() {
        val prompt = client.buildPrompt(
            AiRecommendationInput(
                availableBreedNames = listOf("akita", "poodle"),
                favoriteBreedNames = emptyList(),
                limit = 2,
                languageTag = "ru-RU",
                strictLanguage = true,
            ),
        )

        assertTrue(prompt.contains("every explanation in Russian"))
        assertTrue(prompt.contains("corrective retry"))
    }
}
