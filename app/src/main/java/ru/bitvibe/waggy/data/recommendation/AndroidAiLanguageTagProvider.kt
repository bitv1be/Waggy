package ru.bitvibe.waggy.data.recommendation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.bitvibe.waggy.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAiLanguageTagProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : AiLanguageTagProvider {
    override fun currentLanguageTag(): String {
        return SupportedAiLanguage.fromLanguageTag(
            context.getString(R.string.ai_output_language_tag),
        ).languageTag
    }
}
