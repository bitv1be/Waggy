package ru.bitvibe.waggy.data.recommendation

import ru.bitvibe.waggy.domain.models.BreedRecommendation
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.models.RecommendationReason
import ru.bitvibe.waggy.domain.models.RecommendationResult
import ru.bitvibe.waggy.domain.models.RecommendationSource
import ru.bitvibe.waggy.domain.repository.RecommendationProvider
import ru.bitvibe.waggy.domain.repository.RecommendationRequest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiRecommendationProvider @Inject constructor(
    private val aiClient: BreedRecommendationAiClient,
    private val languageTagProvider: AiLanguageTagProvider,
    private val languageValidator: AiResponseLanguageValidator,
) : RecommendationProvider {
    private val cache = LinkedHashMap<RecommendationCacheKey, RecommendationResult>()

    override suspend fun getRecommendations(request: RecommendationRequest): RecommendationResult {
        if (request.availableBreeds.isEmpty()) {
            return RecommendationResult(
                recommendations = emptyList(),
                source = request.source,
            )
        }

        val languageTag = languageTagProvider.currentLanguageTag()
        val cacheKey = request.toCacheKey(languageTag)
        if (!request.forceRefresh) {
            synchronized(cache) { cache[cacheKey] }?.let { return it }
        }

        val availableBreedsByName = request.availableBreeds.associateBy { it.name.canonicalName() }
        val favoriteParentNames = request.favorites
            .map { it.breedName.canonicalName() }
            .toSet()
        val input = AiRecommendationInput(
            availableBreedNames = request.availableBreeds.map { it.name },
            favoriteBreedNames = request.favorites.map { favorite -> favorite.displayName() },
            limit = request.limit,
            languageTag = languageTag,
        )
        var aiRecommendations = aiClient.generateRecommendations(input)
        var usesExpectedLanguage = aiRecommendations.useExpectedLanguage(languageTag)
        if (!usesExpectedLanguage) {
            aiRecommendations = aiClient.generateRecommendations(input.copy(strictLanguage = true))
            usesExpectedLanguage = aiRecommendations.useExpectedLanguage(languageTag)
        }
        require(usesExpectedLanguage) {
            "Firebase AI returned recommendation explanations in the wrong language."
        }
        val recommendations = aiRecommendations
            .asSequence()
            .mapNotNull { candidate ->
                val breed = availableBreedsByName[candidate.breedName.canonicalName()]
                    ?: return@mapNotNull null
                val explanation = candidate.explanation.trim()
                if (breed.name.canonicalName() in favoriteParentNames || explanation.isEmpty()) {
                    return@mapNotNull null
                }
                BreedRecommendation(
                    breed = breed,
                    reason = RecommendationReason.AiGenerated(
                        explanation = explanation.take(MAX_EXPLANATION_LENGTH),
                    ),
                )
            }
            .distinctBy { it.breed.name.canonicalName() }
            .take(request.limit)
            .toList()
        val result = RecommendationResult(
            recommendations = recommendations,
            source = request.source,
        )
        synchronized(cache) {
            cache[cacheKey] = result
            while (cache.size > MAX_CACHE_ENTRIES) {
                cache.remove(cache.keys.first())
            }
        }
        return result
    }

    private val RecommendationRequest.source: RecommendationSource
        get() = if (favorites.isEmpty()) {
            RecommendationSource.POPULAR
        } else {
            RecommendationSource.PERSONALIZED
        }

    private fun RecommendationRequest.toCacheKey(languageTag: String): RecommendationCacheKey {
        return RecommendationCacheKey(
            availableBreedNames = availableBreeds.map { it.name.canonicalName() }.sorted(),
            favoriteBreedNames = favorites
                .map { favorite -> favorite.displayName().canonicalName() }
                .sorted(),
            limit = limit,
            languageTag = languageTag,
        )
    }

    private fun Favorite.displayName(): String {
        return listOfNotNull(subBreedName, breedName).joinToString(separator = " ")
    }

    private suspend fun List<AiBreedRecommendation>.useExpectedLanguage(
        languageTag: String,
    ): Boolean {
        return asSequence()
            .map { recommendation -> recommendation.explanation.trim() }
            .filter(String::isNotEmpty)
            .all { explanation ->
                languageValidator.matches(explanation, languageTag)
            }
    }

    private fun String.canonicalName(): String {
        return lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)
    }

    private data class RecommendationCacheKey(
        val availableBreedNames: List<String>,
        val favoriteBreedNames: List<String>,
        val limit: Int,
        val languageTag: String,
    )

    private companion object {
        const val MAX_CACHE_ENTRIES = 8
        const val MAX_EXPLANATION_LENGTH = 180
    }
}
