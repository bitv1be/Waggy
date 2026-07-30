package ru.bitvibe.waggy.data.recommendation

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MlKitAiResponseLanguageValidator @Inject constructor() : AiResponseLanguageValidator {
    private val languageIdentifier by lazy {
        LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(MINIMUM_CONFIDENCE)
                .build(),
        )
    }

    override suspend fun matches(
        text: String,
        expectedLanguageTag: String,
    ): Boolean {
        val detectedLanguageTag = identifyLanguage(text)
        return detectedLanguageTag != UNDETERMINED_LANGUAGE_TAG &&
                detectedLanguageTag.baseLanguage() == expectedLanguageTag.baseLanguage()
    }

    private suspend fun identifyLanguage(text: String): String =
        suspendCancellableCoroutine { continuation ->
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageTag ->
                    if (continuation.isActive) continuation.resume(languageTag)
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
        }

    private fun String.baseLanguage(): String {
        return Locale.forLanguageTag(this).language.lowercase(Locale.ROOT)
    }

    private companion object {
        const val MINIMUM_CONFIDENCE = 0.5f
        const val UNDETERMINED_LANGUAGE_TAG = "und"
    }
}
