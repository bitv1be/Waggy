package ru.bitvibe.waggy.data.recommendation

import java.util.Locale

internal enum class SupportedAiLanguage(
    val languageTag: String,
    val promptName: String,
) {
    ENGLISH(languageTag = "en", promptName = "English"),
    RUSSIAN(languageTag = "ru", promptName = "Russian"),
    ;

    companion object {
        fun fromLocale(locale: Locale): SupportedAiLanguage {
            return if (locale.language.equals(RUSSIAN.languageTag, ignoreCase = true)) {
                RUSSIAN
            } else {
                ENGLISH
            }
        }

        fun fromLanguageTag(languageTag: String): SupportedAiLanguage {
            return fromLocale(Locale.forLanguageTag(languageTag))
        }
    }
}
