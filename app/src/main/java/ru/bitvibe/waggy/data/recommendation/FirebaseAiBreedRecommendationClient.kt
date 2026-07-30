package ru.bitvibe.waggy.data.recommendation

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.bitvibe.waggy.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiBreedRecommendationClient @Inject constructor() : BreedRecommendationAiClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val responseSchema = Schema.obj(
        mapOf(
            "recommendations" to Schema.array(
                Schema.obj(
                    mapOf(
                        "breedName" to Schema.string(),
                        "explanation" to Schema.string(),
                    ),
                ),
            ),
        ),
    )
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = BuildConfig.FIREBASE_AI_MODEL,
            generationConfig = generationConfig {
                responseMimeType = JSON_MIME_TYPE
                responseSchema = this@FirebaseAiBreedRecommendationClient.responseSchema
            },
        )
    }

    override suspend fun generateRecommendations(
        input: AiRecommendationInput,
    ): List<AiBreedRecommendation> {
        val responseText = model.generateContent(buildPrompt(input)).text
            ?.takeIf(String::isNotBlank)
            ?: error("Firebase AI returned an empty recommendation response.")
        return parseResponse(responseText)
    }

    internal fun parseResponse(responseText: String): List<AiBreedRecommendation> {
        return json.decodeFromString<AiRecommendationEnvelope>(responseText)
            .recommendations
            .map { recommendation ->
                AiBreedRecommendation(
                    breedName = recommendation.breedName,
                    explanation = recommendation.explanation,
                )
            }
    }

    internal fun buildPrompt(input: AiRecommendationInput): String {
        val outputLanguage = SupportedAiLanguage.fromLanguageTag(input.languageTag)
        val payload = RecommendationPromptPayload(
            availableBreedNames = input.availableBreedNames,
            favoriteBreedNames = input.favoriteBreedNames,
            maximumRecommendations = input.limit,
            explanationLanguage = outputLanguage.promptName,
        )
        val correctionInstruction = if (input.strictLanguage) {
            "This is a corrective retry because the previous explanations used the wrong language."
        } else {
            ""
        }
        return """
            You are Waggy's dog breed recommendation expert.
            Select at most ${input.limit} breeds only from the live catalog in the JSON input.
            Return each breedName exactly as it appears in availableBreedNames.
            Never return a breed whose parent breed appears in favoriteBreedNames.
            When favorites are present, compare size, temperament, activity, trainability,
            grooming needs, and lifestyle compatibility.
            When favorites are empty, choose broadly popular breeds from the live catalog.
            Give each result one clear, friendly explanation of at least 3 lines. Write every
            word of every explanation in ${outputLanguage.promptName}; do not mix languages.
            Breed names must remain exact catalog identifiers and may remain in English.
            $correctionInstruction
            Do not include markdown.

            Input JSON:
            ${json.encodeToString(payload)}
        """.trimIndent()
    }

    @Serializable
    private data class RecommendationPromptPayload(
        val availableBreedNames: List<String>,
        val favoriteBreedNames: List<String>,
        val maximumRecommendations: Int,
        val explanationLanguage: String,
    )

    @Serializable
    private data class AiRecommendationEnvelope(
        val recommendations: List<AiRecommendationItem>,
    )

    @Serializable
    private data class AiRecommendationItem(
        val breedName: String,
        val explanation: String,
    )

    private companion object {
        const val JSON_MIME_TYPE = "application/json"
    }
}
