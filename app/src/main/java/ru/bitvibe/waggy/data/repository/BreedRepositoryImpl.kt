package ru.bitvibe.waggy.data.repository

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.bitvibe.waggy.data.local.BreedEntity
import ru.bitvibe.waggy.data.local.BreedsDao
import ru.bitvibe.waggy.data.local.SubBreedEntity
import ru.bitvibe.waggy.data.local.toDomainList
import ru.bitvibe.waggy.data.remote.BreedsApi
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.models.RandomFavoriteBreed
import ru.bitvibe.waggy.domain.repository.BreedRepository
import javax.inject.Inject

class BreedRepositoryImpl @Inject constructor(
    private val api: BreedsApi,
    private val dao: BreedsDao,
) : BreedRepository {

    override suspend fun getAll(forceRefresh: Boolean): List<Breed> = coroutineScope {
        val cached = dao.getAllBreedsWithSubBreeds()
        if (cached.isNotEmpty() && !forceRefresh) {
            return@coroutineScope cached.toDomainList()
        }

        val remote = api.getAll()

        if (remote.status == SUCCESS_STATUS) {
            val breedEntities = mutableListOf<BreedEntity>()
            val subBreedEntities = mutableListOf<SubBreedEntity>()

            val deferredBreeds = remote.message.keys.map { breedName ->
                async {
                    val imageUrl = try {
                        val imageResponse = api.getRandomImageForBreed(breedName)
                        imageResponse.message.takeIf {
                            imageResponse.status == SUCCESS_STATUS && it.isNotBlank()
                        }
                    } catch (e: Exception) {
                        val message = e.message ?: "Unknown error"
                        Firebase.crashlytics.log(message)
                        Firebase.crashlytics.recordException(e)
                        null
                    }
                    BreedEntity(breedName = breedName, imageUrl = imageUrl)
                }
            }

            breedEntities.addAll(deferredBreeds.awaitAll())

            remote.message.forEach { (breedName, subBreedsList) ->
                subBreedsList.forEach { subBreedName ->
                    subBreedEntities.add(
                        SubBreedEntity(
                            parentBreedName = breedName,
                            subBreedName = subBreedName,
                        ),
                    )
                }
            }

            dao.refreshCache(breedEntities, subBreedEntities)
        }

        return@coroutineScope dao.getAllBreedsWithSubBreeds().toDomainList()
    }

    override suspend fun getByName(name: String): Breed? {
        val cached = dao.getBreedByName(name)
        return cached?.toDomain()
    }

    override suspend fun getAllFavorites(): List<Favorite> {
        val result = dao.getAllFavorites()
        return result.map { it.toModel() }
    }

    override fun observeAllFavorites(): Flow<List<Favorite>> {
        return dao.observeAllFavorites().map { favorites ->
            favorites.map { it.toModel() }
        }
    }

    override suspend fun toggleBreedFavorite(
        breed: Favorite,
        isFavorite: Boolean
    ) {
        if (isFavorite) {
            dao.insertFavorite(breed.toEntity())
        } else {
            dao.deleteFavoriteByName(breed.breedName, breed.subBreedName)
        }
    }

    override suspend fun clearAllFavorites() {
        dao.clearAllFavorites()
    }

    override suspend fun getRandomFavoriteBreed(): RandomFavoriteBreed? {
        val favorites = dao.getAllFavorites()
        if (favorites.isEmpty()) return null

        val randomFavorite = favorites.random()
        val imageUrl = try {
            if (randomFavorite.subBreedName != null) {
                api.getRandomImageForSubBreed(
                    randomFavorite.breedName,
                    randomFavorite.subBreedName
                ).message
            } else {
                api.getRandomImageForBreed(randomFavorite.breedName).message
            }
        } catch (e: Exception) {
            val message = e.message ?: "Unknown error"
            Firebase.crashlytics.log(message)
            Firebase.crashlytics.recordException(e)
            return null
        }

        return RandomFavoriteBreed(
            breedName = randomFavorite.breedName,
            subBreedName = randomFavorite.subBreedName,
            imageUrl = imageUrl
        )
    }

    private companion object {
        const val SUCCESS_STATUS = "success"
    }
}
