package ru.bitvibe.waggy.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface BreedsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreeds(breeds: List<BreedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubBreeds(subBreeds: List<SubBreedEntity>)

    @Transaction
    suspend fun refreshCache(breeds: List<BreedEntity>, subBreeds: List<SubBreedEntity>) {
        insertBreeds(breeds)
        insertSubBreeds(subBreeds)
    }

    @Transaction
    @Query("SELECT * FROM breeds ORDER BY breedName ASC")
    suspend fun getAllBreedsWithSubBreeds(): List<BreedWithSubBreeds>

    @Transaction
    @Query("SELECT * FROM breeds WHERE breedName LIKE '%' || :name || '%' LIMIT 1")
    suspend fun getBreedByName(name: String): BreedWithSubBreeds?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_breeds WHERE breedName = :breedName AND subBreedName IS :subBreedName")
    suspend fun deleteFavoriteByName(breedName: String, subBreedName: String?)

    @Transaction
    @Query("SELECT * FROM favorite_breeds")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Query("DELETE FROM favorite_breeds")
    suspend fun clearAllFavorites()
}
