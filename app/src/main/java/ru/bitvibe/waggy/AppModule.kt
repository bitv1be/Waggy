package ru.bitvibe.waggy

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.bitvibe.waggy.data.local.AppDatabase
import ru.bitvibe.waggy.data.remote.GitHubReleaseApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val contentType = "application/json".toMediaType()
    private val json = Json {
        allowComments = true
        ignoreUnknownKeys = true
        allowTrailingComma = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

    @Provides
    @Singleton
    fun provideGitHubReleaseApi(): GitHubReleaseApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.GITHUB_RELEASES_API_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GitHubReleaseApi::class.java)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "waggy.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ru.bitvibe.waggy.domain.preferences.ThemePreferences {
        return ru.bitvibe.waggy.data.preferences.ThemePreferencesImpl(context)
    }

    @Provides
    @Singleton
    fun provideWidgetPreferences(@ApplicationContext context: Context): ru.bitvibe.waggy.domain.preferences.WidgetPreferences {
        return ru.bitvibe.waggy.data.preferences.WidgetPreferencesImpl(context)
    }
}
