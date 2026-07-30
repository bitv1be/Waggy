package ru.bitvibe.waggy.domain.usecase

import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

class GetAllBreedsUseCase @Inject constructor(
    private val repository: BreedRepository,
) : UseCase<Boolean, List<Breed>> {
    override suspend fun invoke(args: Boolean): List<Breed> {
        return repository.getAll(forceRefresh = args)
    }
}
