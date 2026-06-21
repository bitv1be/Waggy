package ru.bitvibe.waggy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.bitvibe.waggy.domain.models.Favorite

@Entity(tableName = "favorite_breeds")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val breedName: String,
    val subBreedName: String? = null
) {
    fun toModel() = Favorite(
        id,
        breedName,
        subBreedName
    )
}