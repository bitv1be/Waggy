package ru.bitvibe.waggy.domain.usecase

import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

class GetAllFavoritesUseCase @Inject constructor(
    private val repository: BreedRepository,
) : UseCase<UseCase.None, List<Favorite>> {
    override suspend fun invoke(args: UseCase.None): List<Favorite> {
        return repository.getAllFavorites()
    }
}