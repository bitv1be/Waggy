package ru.bitvibe.waggy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breeds")
data class BreedEntity(
    @PrimaryKey
    val breedName: String,
    val imageUrl: String?,
)
