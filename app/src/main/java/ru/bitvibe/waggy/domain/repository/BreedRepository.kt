package ru.bitvibe.waggy.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.models.RandomFavoriteBreed

interface BreedRepository {
    suspend fun getAll(forceRefresh: Boolean = false): List<Breed>

    suspend fun getByName(name: String): Breed?

    suspend fun getAllFavorites(): List<Favorite>

    fun observeAllFavorites(): Flow<List<Favorite>>

    suspend fun toggleBreedFavorite(breed: Favorite, isFavorite: Boolean)

    suspend fun clearAllFavorites()

    suspend fun getRandomFavoriteBreed(): RandomFavoriteBreed?
}
