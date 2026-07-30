package ru.bitvibe.waggy.data.recommendation

import ru.bitvibe.waggy.domain.repository.BreedDescriptionProvider
import ru.bitvibe.waggy.domain.repository.BreedDescriptionRequest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiBreedDescriptionProvider @Inject constructor(
    private val aiClient: BreedDescriptionAiClient,
    private val languageTagProvider: AiLanguageTagProvider,
    private val languageValidator: AiResponseLanguageValidator,
) : BreedDescriptionProvider {
    private val cache = LinkedHashMap<BreedDescriptionCacheKey, String>()

    override suspend fun getDescription(request: BreedDescriptionRequest): String {
        val languageTag = languageTagProvider.currentLanguageTag()
        val cacheKey = BreedDescriptionCacheKey(
            breedName = request.breed.name.canonicalName(),
            subBreedNames = request.breed.subBreeds.map { it.canonicalName() }.sorted(),
            languageTag = languageTag,
        )
        if (!request.forceRefresh) {
            synchronized(cache) { cache[cacheKey] }?.let { return it }
        }

        val input = AiBreedDescriptionInput(
            breedName = request.breed.name,
            subBreedNames = request.breed.subBreeds,
            languageTag = languageTag,
        )
        var description = aiClient.generateDescription(input).trim()
        var usesExpectedLanguage = description.isNotEmpty() &&
                languageValidator.matches(description, languageTag)
        if (!usesExpectedLanguage) {
            description = aiClient.generateDescription(input.copy(strictLanguage = true)).trim()
            usesExpectedLanguage = description.isNotEmpty() &&
                    languageValidator.matches(description, languageTag)
        }
        require(description.isNotEmpty()) { "Firebase AI returned a blank breed description." }
        require(usesExpectedLanguage) {
            "Firebase AI returned a breed description in the wrong language."
        }
        val safeDescription = description.take(MAX_DESCRIPTION_LENGTH)

        synchronized(cache) {
            cache[cacheKey] = safeDescription
            while (cache.size > MAX_CACHE_ENTRIES) {
                cache.remove(cache.keys.first())
            }
        }
        return safeDescription
    }

    private fun String.canonicalName(): String {
        return lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)
    }

    private data class BreedDescriptionCacheKey(
        val breedName: String,
        val subBreedNames: List<String>,
        val languageTag: String,
    )

    private companion object {
        const val MAX_CACHE_ENTRIES = 64
        const val MAX_DESCRIPTION_LENGTH = 800
    }
}
