package ru.bitvibe.waggy.data.recommendation

class FakeAiResponseLanguageValidator(
    private val matcher: (text: String, expectedLanguageTag: String) -> Boolean = { _, _ -> true },
) : AiResponseLanguageValidator {
    val validations = mutableListOf<Pair<String, String>>()

    override suspend fun matches(
        text: String,
        expectedLanguageTag: String,
    ): Boolean {
        validations += text to expectedLanguageTag
        return matcher(text, expectedLanguageTag)
    }
}

class FakeAiLanguageTagProvider(
    private val languageTag: String = "en",
) : AiLanguageTagProvider {
    override fun currentLanguageTag(): String = languageTag
}
