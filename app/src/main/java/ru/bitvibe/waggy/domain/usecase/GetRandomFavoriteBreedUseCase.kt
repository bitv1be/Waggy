package ru.bitvibe.waggy.domain.usecase

import ru.bitvibe.waggy.domain.models.RandomFavoriteBreed
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

class GetRandomFavoriteBreedUseCase @Inject constructor(
    private val repository: BreedRepository
) : UseCase<UseCase.None, RandomFavoriteBreed?> {
    override suspend fun invoke(args: UseCase.None): RandomFavoriteBreed? {
        return repository.getRandomFavoriteBreed()
    }
}
