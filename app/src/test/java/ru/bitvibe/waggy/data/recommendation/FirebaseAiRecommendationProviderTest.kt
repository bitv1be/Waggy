package ru.bitvibe.waggy.data.recommendation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.models.RecommendationReason
import ru.bitvibe.waggy.domain.models.RecommendationSource
import ru.bitvibe.waggy.domain.repository.RecommendationRequest

class FirebaseAiRecommendationProviderTest {
    @Test
    fun personalizedRequest_usesLiveCatalogAndMapsAiSelectionToOriginalBreed() = runBlocking {
        val selectedBreed = breed("poodle", imageUrl = "https://example.test/poodle.jpg")
        val aiClient = FakeBreedRecommendationAiClient(
            recommendations = listOf(
                AiBreedRecommendation(
                    breedName = "Poodle",
                    explanation = "Matches the activity and training style you already enjoy.",
                ),
            ),
        )
        val provider = provider(aiClient)

        val result = provider.getRecommendations(
            RecommendationRequest(
                availableBreeds = listOf(breed("retriever"), selectedBreed),
                favorites = listOf(Favorite(breedName = "retriever", subBreedName = "golden")),
                limit = 3,
            ),
        )

        assertEquals(RecommendationSource.PERSONALIZED, result.source)
        assertSame(selectedBreed, result.recommendations.single().breed)
        assertEquals(
            RecommendationReason.AiGenerated(
                "Matches the activity and training style you already enjoy.",
            ),
            result.recommendations.single().reason,
        )
        assertEquals(
            listOf("retriever", "poodle"),
            aiClient.inputs.single().availableBreedNames,
        )
        assertEquals(listOf("golden retriever"), aiClient.inputs.single().favoriteBreedNames)
    }

    @Test
    fun aiOutput_filtersFavoritesUnknownBreedsDuplicatesAndBlankExplanations() = runBlocking {
        val aiClient = FakeBreedRecommendationAiClient(
            recommendations = listOf(
                AiBreedRecommendation("Hound", "Already a favorite."),
                AiBreedRecommendation("Poodle", "A smart and adaptable companion."),
                AiBreedRecommendation("pood-le", "Duplicate spelling."),
                AiBreedRecommendation("Unknown", "Not in the web catalog."),
                AiBreedRecommendation("Akita", "   "),
            ),
        )
        val provider = provider(aiClient)

        val result = provider.getRecommendations(
            RecommendationRequest(
                availableBreeds = breeds("hound", "poodle", "akita"),
                favorites = listOf(Favorite(breedName = "HOUND", subBreedName = "afghan")),
            ),
        )

        assertEquals(listOf("poodle"), result.recommendations.map { it.breed.name })
    }

    @Test
    fun emptyFavorites_usesAiPopularSourceWithoutAStaticBreedList() = runBlocking {
        val aiClient = FakeBreedRecommendationAiClient(
            recommendations = listOf(AiBreedRecommendation("akita", "A widely admired companion.")),
        )
        val provider = provider(aiClient)

        val result = provider.getRecommendations(
            RecommendationRequest(
                availableBreeds = breeds("akita", "beagle"),
                favorites = emptyList(),
                limit = 1,
            ),
        )

        assertEquals(RecommendationSource.POPULAR, result.source)
        assertEquals(listOf("akita"), result.recommendations.map { it.breed.name })
        assertTrue(aiClient.inputs.single().favoriteBreedNames.isEmpty())
    }

    @Test
    fun repeatedRequest_usesCacheUntilRefreshIsForced() = runBlocking {
        val aiClient = FakeBreedRecommendationAiClient(
            recommendations = listOf(AiBreedRecommendation("akita", "A strong match.")),
        )
        val provider = provider(aiClient)
        val request = RecommendationRequest(
            availableBreeds = breeds("akita", "beagle"),
            favorites = listOf(Favorite(breedName = "beagle")),
        )

        provider.getRecommendations(request)
        provider.getRecommendations(request)
        provider.getRecommendations(request.copy(forceRefresh = true))

        assertEquals(2, aiClient.inputs.size)
    }

    @Test
    fun emptyLiveCatalog_returnsEmptyWithoutCallingAi() = runBlocking {
        val aiClient = FakeBreedRecommendationAiClient(emptyList())
        val provider = provider(aiClient)

        val result = provider.getRecommendations(
            RecommendationRequest(
                availableBreeds = emptyList(),
                favorites = emptyList(),
            ),
        )

        assertTrue(result.recommendations.isEmpty())
        assertTrue(aiClient.inputs.isEmpty())
    }

    @Test
    fun wrongLanguageExplanation_retriesAndNeverReturnsTheMismatch() {
        val aiClient = FakeBreedRecommendationAiClient(
            recommendations = listOf(
                AiBreedRecommendation("akita", "A calm and loyal companion."),
            ),
            additionalResponses = listOf(
                listOf(AiBreedRecommendation("akita", "Спокойный и преданный компаньон.")),
            ),
        )
        val validator = FakeAiResponseLanguageValidator { text, languageTag ->
            languageTag == "ru" && text.any { character -> character in 'А'..'я' }
        }
        val provider = FirebaseAiRecommendationProvider(
            aiClient,
            FakeAiLanguageTagProvider("ru"),
            validator,
        )

        val result = runBlocking {
            provider.getRecommendations(
                RecommendationRequest(
                    availableBreeds = breeds("akita", "beagle"),
                    favorites = emptyList(),
                ),
            )
        }

        assertEquals(
            RecommendationReason.AiGenerated("Спокойный и преданный компаньон."),
            result.recommendations.single().reason,
        )
        assertEquals(2, aiClient.inputs.size)
        assertEquals("ru", aiClient.inputs.first().languageTag)
        assertTrue(aiClient.inputs.last().strictLanguage)
    }

    private fun provider(
        aiClient: BreedRecommendationAiClient,
    ): FirebaseAiRecommendationProvider {
        return FirebaseAiRecommendationProvider(
            aiClient,
            FakeAiLanguageTagProvider(),
            FakeAiResponseLanguageValidator(),
        )
    }

    private fun breeds(vararg names: String): List<Breed> = names.map { name -> breed(name) }

    private fun breed(name: String, imageUrl: String? = null): Breed {
        return Breed(name = name, imageUrl = imageUrl, subBreeds = emptyList())
    }
}

private class FakeBreedRecommendationAiClient(
    recommendations: List<AiBreedRecommendation>,
    additionalResponses: List<List<AiBreedRecommendation>> = emptyList(),
) : BreedRecommendationAiClient {
    private val responses = listOf(recommendations) + additionalResponses

    val inputs = mutableListOf<AiRecommendationInput>()

    override suspend fun generateRecommendations(
        input: AiRecommendationInput,
    ): List<AiBreedRecommendation> {
        inputs += input
        return responses.getOrElse(inputs.lastIndex) { responses.last() }
    }
}
