package ru.bitvibe.waggy.domain.repository

import ru.bitvibe.waggy.domain.models.Breed

data class BreedDescriptionRequest(
    val breed: Breed,
    val forceRefresh: Boolean = false,
)

interface BreedDescriptionProvider {
    suspend fun getDescription(request: BreedDescriptionRequest): String
}
