package ru.bitvibe.waggy.domain.repository

import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.models.RandomFavoriteBreed

interface BreedRepository {
    suspend fun getAll(): List<Breed>

    suspend fun getByName(name: String): Breed?

    suspend fun getAllFavorites(): List<Favorite>
    
    suspend fun toggleBreedFavorite(breed: Favorite, isFavorite: Boolean)

    suspend fun clearAllFavorites()

    suspend fun getRandomFavoriteBreed(): RandomFavoriteBreed?
}