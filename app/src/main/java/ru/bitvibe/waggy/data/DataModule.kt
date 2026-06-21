package ru.bitvibe.waggy.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ru.bitvibe.waggy.data.local.AppDatabase
import ru.bitvibe.waggy.data.local.BreedsDao
import ru.bitvibe.waggy.data.remote.BreedsApi
import ru.bitvibe.waggy.data.repository.BreedRepositoryImpl
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindBreedRepository(
        repository: BreedRepositoryImpl
    ): BreedRepository

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