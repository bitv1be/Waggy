package ru.bitvibe.waggy.data.recommendation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.repository.BreedDescriptionRequest

class FirebaseAiBreedDescriptionProviderTest {
    @Test
    fun description_usesBreedDataAndTrimsAiResponse() = runBlocking {
        val aiClient = FakeBreedDescriptionAiClient("  Friendly and adaptable.  ")
        val provider = FirebaseAiBreedDescriptionProvider(
            aiClient,
            FakeAiLanguageTagProvider(),
            FakeAiResponseLanguageValidator(),
        )

        val description = provider.getDescription(
            BreedDescriptionRequest(
                breed = Breed(
                    name = "retriever",
                    imageUrl = null,
                    subBreeds = listOf("golden", "chesapeake"),
                ),
            ),
        )

        assertEquals("Friendly and adaptable.", description)
        assertEquals("retriever", aiClient.inputs.single().breedName)
        assertEquals(listOf("golden", "chesapeake"), aiClient.inputs.single().subBreedNames)
    }

    @Test
    fun repeatedRequest_usesCacheUntilRefreshIsForced() = runBlocking {
        val aiClient = FakeBreedDescriptionAiClient("A thoughtful breed overview.")
        val provider = FirebaseAiBreedDescriptionProvider(
            aiClient,
            FakeAiLanguageTagProvider(),
            FakeAiResponseLanguageValidator(),
        )
        val request = BreedDescriptionRequest(
            breed = Breed("hound", null, emptyList()),
        )

        provider.getDescription(request)
        provider.getDescription(request)
        provider.getDescription(request.copy(forceRefresh = true))

        assertEquals(2, aiClient.inputs.size)
    }

    @Test
    fun blankAiResponse_isRejected() {
        val provider = FirebaseAiBreedDescriptionProvider(
            FakeBreedDescriptionAiClient("   "),
            FakeAiLanguageTagProvider(),
            FakeAiResponseLanguageValidator(),
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                provider.getDescription(
                    BreedDescriptionRequest(Breed("terrier", null, emptyList())),
                )
            }
        }
    }

    @Test
    fun wrongLanguageResponse_retriesWithStrictLanguageAndReturnsCorrection() {
        val aiClient = FakeBreedDescriptionAiClient(
            "Friendly and adaptable.",
            "Дружелюбная и легко адаптирующаяся порода.",
        )
        val validator = FakeAiResponseLanguageValidator { text, languageTag ->
            languageTag == "ru" && text.any { character -> character in 'А'..'я' }
        }
        val provider = FirebaseAiBreedDescriptionProvider(
            aiClient,
            FakeAiLanguageTagProvider("ru"),
            validator,
        )

        val description = runBlocking {
            provider.getDescription(
                BreedDescriptionRequest(Breed("poodle", null, emptyList())),
            )
        }

        assertEquals("Дружелюбная и легко адаптирующаяся порода.", description)
        assertEquals(2, aiClient.inputs.size)
        assertEquals("ru", aiClient.inputs.first().languageTag)
        assertTrue(aiClient.inputs.last().strictLanguage)
    }

    @Test
    fun wrongLanguageCorrection_isRejectedInsteadOfBeingDisplayed() {
        val aiClient = FakeBreedDescriptionAiClient(
            "Descripción en un idioma incorrecto.",
            "La respuesta corregida sigue siendo incorrecta.",
        )
        val provider = FirebaseAiBreedDescriptionProvider(
            aiClient,
            FakeAiLanguageTagProvider(),
            FakeAiResponseLanguageValidator { _, _ -> false },
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                provider.getDescription(
                    BreedDescriptionRequest(Breed("poodle", null, emptyList())),
                )
            }
        }
        assertEquals(2, aiClient.inputs.size)
        assertTrue(aiClient.inputs.last().strictLanguage)
    }
}

private class FakeBreedDescriptionAiClient(
    vararg descriptions: String,
) : BreedDescriptionAiClient {
    private val descriptions = descriptions.toList()
    val inputs = mutableListOf<AiBreedDescriptionInput>()

    override suspend fun generateDescription(input: AiBreedDescriptionInput): String {
        inputs += input
        return descriptions.getOrElse(inputs.lastIndex) { descriptions.last() }
    }
}
