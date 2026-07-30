package ru.bitvibe.waggy.data.recommendation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAiBreedDescriptionClientTest {
    private val client = FirebaseAiBreedDescriptionClient()

    @Test
    fun parseResponse_readsStructuredDescriptionJson() {
        val description = client.parseResponse(
            """
                {
                  "description": "Friendly, active, and responsive to patient training."
                }
            """.trimIndent(),
        )

        assertEquals(
            "Friendly, active, and responsive to patient training.",
            description,
        )
    }

    @Test
    fun buildPrompt_mapsUnsupportedLocaleToEnglishUiFallback() {
        val prompt = client.buildPrompt(
            AiBreedDescriptionInput(
                breedName = "poodle",
                subBreedNames = emptyList(),
                languageTag = "es-ES",
            ),
        )

        assertTrue(prompt.contains("every word of the description in English"))
        assertFalse(prompt.contains("in Spanish"))
    }

    @Test
    fun buildPrompt_marksWrongLanguageRetryAsCorrective() {
        val prompt = client.buildPrompt(
            AiBreedDescriptionInput(
                breedName = "poodle",
                subBreedNames = emptyList(),
                languageTag = "ru",
                strictLanguage = true,
            ),
        )

        assertTrue(prompt.contains("every word of the description in Russian"))
        assertTrue(prompt.contains("corrective retry"))
    }
}
