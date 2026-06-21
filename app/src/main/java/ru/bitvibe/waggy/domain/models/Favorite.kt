package ru.bitvibe.waggy.domain.models

import ru.bitvibe.waggy.data.local.FavoriteEntity

data class Favorite(
    val id: Long = 0,
    val breedName: String,
    val subBreedName: String? = null
) {
    fun toEntity() = FavoriteEntity(
        id,
        breedName,
        subBreedName
    )
}
