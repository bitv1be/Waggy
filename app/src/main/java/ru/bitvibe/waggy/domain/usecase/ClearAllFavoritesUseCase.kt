package ru.bitvibe.waggy.domain.usecase

import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

class ClearAllFavoritesUseCase @Inject constructor(
    private val repository: BreedRepository
) : UseCase<UseCase.None, UseCase.None> {
    override suspend fun invoke(args: UseCase.None): UseCase.None {
        repository.clearAllFavorites()
        return UseCase.None
    }
}
