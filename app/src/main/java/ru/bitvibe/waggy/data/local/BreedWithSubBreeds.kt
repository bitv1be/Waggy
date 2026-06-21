package ru.bitvibe.waggy.data.local

import androidx.room.Embedded
import androidx.room.Relation
import ru.bitvibe.waggy.domain.models.Breed

data class BreedWithSubBreeds(
    @Embedded
    val breed: BreedEntity,
    @Relation(
        parentColumn = "breedName",
        entityColumn = "parentBreedName"
    )
    val subBreeds: List<SubBreedEntity>
) {
    fun toDomain(): Breed = Breed(
        name = breed.breedName,
        imageUrl = breed.imageUrl,
        subBreeds = subBreeds.map { it.subBreedName }
    )
}

fun List<BreedWithSubBreeds>.toDomainList(): List<Breed> {
    return this.map { it.toDomain() }
}