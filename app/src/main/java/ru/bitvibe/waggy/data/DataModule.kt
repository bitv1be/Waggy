package ru.bitvibe.waggy.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ru.bitvibe.waggy.data.local.AppDatabase
import ru.bitvibe.waggy.data.local.BreedsDao
import ru.bitvibe.waggy.data.recommendation.AiLanguageTagProvider
import ru.bitvibe.waggy.data.recommendation.AiResponseLanguageValidator
import ru.bitvibe.waggy.data.recommendation.AndroidAiLanguageTagProvider
import ru.bitvibe.waggy.data.recommendation.BreedDescriptionAiClient
import ru.bitvibe.waggy.data.recommendation.BreedRecommendationAiClient
import ru.bitvibe.waggy.data.recommendation.FirebaseAiBreedDescriptionClient
import ru.bitvibe.waggy.data.recommendation.FirebaseAiBreedDescriptionProvider
import ru.bitvibe.waggy.data.recommendation.FirebaseAiBreedRecommendationClient
import ru.bitvibe.waggy.data.recommendation.FirebaseAiRecommendationProvider
import ru.bitvibe.waggy.data.recommendation.MlKitAiResponseLanguageValidator
import ru.bitvibe.waggy.data.remote.BreedsApi
import ru.bitvibe.waggy.data.repository.AppUpdateRepositoryImpl
import ru.bitvibe.waggy.data.repository.BreedRepositoryImpl
import ru.bitvibe.waggy.domain.repository.AppUpdateRepository
import ru.bitvibe.waggy.domain.repository.BreedDescriptionProvider
import ru.bitvibe.waggy.domain.repository.BreedRepository
import ru.bitvibe.waggy.domain.repository.RecommendationProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindAppUpdateRepository(
        repository: AppUpdateRepositoryImpl,
    ): AppUpdateRepository

    @Binds
    abstract fun bindBreedRepository(
        repository: BreedRepositoryImpl,
    ): BreedRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationProvider(
        provider: FirebaseAiRecommendationProvider,
    ): RecommendationProvider

    @Binds
    @Singleton
    abstract fun bindBreedRecommendationAiClient(
        client: FirebaseAiBreedRecommendationClient,
    ): BreedRecommendationAiClient

    @Binds
    @Singleton
    abstract fun bindBreedDescriptionProvider(
        provider: FirebaseAiBreedDescriptionProvider,
    ): BreedDescriptionProvider

    @Binds
    @Singleton
    abstract fun bindBreedDescriptionAiClient(
        client: FirebaseAiBreedDescriptionClient,
    ): BreedDescriptionAiClient

    @Binds
    @Singleton
    abstract fun bindAiResponseLanguageValidator(
        validator: MlKitAiResponseLanguageValidator,
    ): AiResponseLanguageValidator

    @Binds
    @Singleton
    abstract fun bindAiLanguageTagProvider(
        provider: AndroidAiLanguageTagProvider,
    ): AiLanguageTagProvider

    companion object {
        @Provides
        @Singleton
        fun provideBreedDao(database: AppDatabase): BreedsDao =
            database.breedDao()

        @Provides
        @Singleton
        fun provideBreedsApi(retrofit: Retrofit): BreedsApi =
            retrofit.create(BreedsApi::class.java)
    }
}
