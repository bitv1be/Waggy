package ru.bitvibe.waggy.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

class ObserveAllFavoritesUseCase @Inject constructor(
    private val repository: BreedRepository,
) {
    operator fun invoke(): Flow<List<Favorite>> = repository.observeAllFavorites()
}
