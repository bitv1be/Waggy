package ru.bitvibe.waggy.domain.usecase

import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

data class ToggleBreedParams(
    val name: String,
    val subName: String? = null,
    val isFavorite: Boolean
)

class ToggleBreedFavoriteUseCase @Inject constructor(
    private val repository: BreedRepository
) : UseCase<ToggleBreedParams, UseCase.None> {
    override suspend fun invoke(args: ToggleBreedParams): UseCase.None {
        repository.toggleBreedFavorite(Favorite(breedName = args.name, subBreedName = args.subName), args.isFavorite)
        return UseCase.None
    }
}