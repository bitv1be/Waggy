package ru.bitvibe.waggy.data.recommendation

import org.junit.Assert.assertEquals
import org.junit.Test

class SupportedAiLanguageTest {
    @Test
    fun russianLanguageTag_usesRussianAiOutput() {
        assertEquals(
            SupportedAiLanguage.RUSSIAN,
            SupportedAiLanguage.fromLanguageTag("ru-RU"),
        )
    }

    @Test
    fun unsupportedLanguageTag_usesEnglishFallback() {
        assertEquals(
            SupportedAiLanguage.ENGLISH,
            SupportedAiLanguage.fromLanguageTag("es-ES"),
        )
    }
}
