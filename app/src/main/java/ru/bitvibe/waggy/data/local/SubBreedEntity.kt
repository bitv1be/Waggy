package ru.bitvibe.waggy.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_breeds",
    foreignKeys = [
        ForeignKey(
            entity = BreedEntity::class,
            parentColumns = ["breedName"],
            childColumns = ["parentBreedName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parentBreedName"])
    ]
)
data class SubBreedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentBreedName: String,
    val subBreedName: String,
)