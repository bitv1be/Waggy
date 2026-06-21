package ru.bitvibe.waggy.presentation.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.bitvibe.waggy.domain.usecase.GetRandomFavoriteBreedUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BreedWidgetEntryPoint {
    fun getRandomUseCase(): GetRandomFavoriteBreedUseCase
}