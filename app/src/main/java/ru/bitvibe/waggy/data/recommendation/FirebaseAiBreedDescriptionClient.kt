package ru.bitvibe.waggy.data.recommendation

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.bitvibe.waggy.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiBreedDescriptionClient @Inject constructor() : BreedDescriptionAiClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val responseSchema = Schema.obj(
        mapOf("description" to Schema.string()),
    )
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = BuildConfig.FIREBASE_AI_MODEL,
            generationConfig = generationConfig {
                responseMimeType = JSON_MIME_TYPE
                responseSchema = this@FirebaseAiBreedDescriptionClient.responseSchema
            },
        )
    }

    override suspend fun generateDescription(input: AiBreedDescriptionInput): String {
        val responseText = model.generateContent(buildPrompt(input)).text
            ?.takeIf(String::isNotBlank)
            ?: error("Firebase AI returned an empty breed description response.")
        return parseResponse(responseText)
    }

    internal fun parseResponse(responseText: String): String {
        return json.decodeFromString<AiBreedDescriptionResponse>(responseText).description
    }

    internal fun buildPrompt(input: AiBreedDescriptionInput): String {
        val outputLanguage = SupportedAiLanguage.fromLanguageTag(input.languageTag)
        val payload = BreedDescriptionPromptPayload(
            breedName = input.breedName,
            subBreedNames = input.subBreedNames,
            explanationLanguage = outputLanguage.promptName,
        )
        val correctionInstruction = if (input.strictLanguage) {
            "This is a corrective retry because the previous response used the wrong language."
        } else {
            ""
        }
        return """
            You are Waggy's dog breed guide.
            Write a friendly, factual overview of this breed in 55 to 75 words.
            Cover typical temperament, activity, training, grooming, and household fit.
            Make it useful to a prospective owner without giving medical advice or making
            absolute claims. Mention that individual dogs can vary when appropriate.
            Write every word of the description in ${outputLanguage.promptName}. Do not mix
            languages. Breed and sub-breed identifiers in the input may remain unchanged.
            $correctionInstruction
            Return no markdown.

            Input JSON:
            ${json.encodeToString(payload)}
        """.trimIndent()
    }

    @Serializable
    private data class BreedDescriptionPromptPayload(
        val breedName: String,
        val subBreedNames: List<String>,
        val explanationLanguage: String,
    )

    @Serializable
    private data class AiBreedDescriptionResponse(
        val description: String,
    )

    private companion object {
        const val JSON_MIME_TYPE = "application/json"
    }
}
