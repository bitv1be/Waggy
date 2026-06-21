package ru.bitvibe.waggy.domain.usecase

import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

class GetBreedByNameUseCase @Inject constructor(
    private val repository: BreedRepository
) : UseCase<String, Breed?> {
    override suspend fun invoke(args: String): Breed? {
        return repository.getByName(args)
    }
}