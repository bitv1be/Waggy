package ru.bitvibe.waggy.data.remote

import kotlinx.serialization.Serializable
import ru.bitvibe.waggy.data.local.BreedEntity
import ru.bitvibe.waggy.data.local.SubBreedEntity
import ru.bitvibe.waggy.domain.models.Breed

@Serializable
data class AllBreedsResponse(
    val message: Map<String, List<String>>,
    val status: String
) {
    fun toModels(): List<Breed> {
        return message.map { (breedName, subBreeds) ->
            Breed(name = breedName, subBreeds = subBreeds, imageUrl = "")
        }
    }

    fun toEntities(): Pair<List<BreedEntity>, List<SubBreedEntity>> {
        val breedEntities = mutableListOf<BreedEntity>()
        val subBreedEntities = mutableListOf<SubBreedEntity>()

        message.forEach { (breedName, subBreedsList) ->
            breedEntities.add(BreedEntity(breedName = breedName, imageUrl = ""))

            subBreedsList.forEach { subBreedName ->
                subBreedEntities.add(
                    SubBreedEntity(
                        parentBreedName = breedName,
                        subBreedName = subBreedName
                    )
                )
            }
        }

        return Pair(breedEntities, subBreedEntities)
    }
}
