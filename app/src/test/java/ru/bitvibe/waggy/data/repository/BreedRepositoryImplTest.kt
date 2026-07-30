package ru.bitvibe.waggy.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.bitvibe.waggy.data.local.BreedEntity
import ru.bitvibe.waggy.data.local.BreedWithSubBreeds
import ru.bitvibe.waggy.data.local.BreedsDao
import ru.bitvibe.waggy.data.local.FavoriteEntity
import ru.bitvibe.waggy.data.local.SubBreedEntity
import ru.bitvibe.waggy.data.remote.AllBreedsResponse
import ru.bitvibe.waggy.data.remote.BreedImageResponse
import ru.bitvibe.waggy.data.remote.BreedsApi

class BreedRepositoryImplTest {
    @Test
    fun cachedCatalog_isUsedWithoutWebRequest() = runBlocking {
        val dao = FakeBreedsDao(
            initialBreeds = listOf(BreedEntity("cached", "https://example.test/cached.jpg")),
        )
        val api = FakeBreedsApi(
            response = AllBreedsResponse(
                message = mapOf("remote" to emptyList()),
                status = "success",
            ),
        )
        val repository = BreedRepositoryImpl(api, dao)

        val breeds = repository.getAll()

        assertEquals(listOf("cached"), breeds.map { it.name })
        assertEquals(0, api.getAllCalls)
    }

    @Test
    fun forcedRefresh_fetchesWebCatalogAndReplacesCachedBreeds() = runBlocking {
        val dao = FakeBreedsDao(
            initialBreeds = listOf(BreedEntity("cached", "https://example.test/cached.jpg")),
            initialSubBreeds = listOf(
                SubBreedEntity(parentBreedName = "cached", subBreedName = "old"),
            ),
        )
        val api = FakeBreedsApi(
            response = AllBreedsResponse(
                message = linkedMapOf("remote" to listOf("mini")),
                status = "success",
            ),
        )
        val repository = BreedRepositoryImpl(api, dao)

        val breeds = repository.getAll(forceRefresh = true)

        assertEquals(1, api.getAllCalls)
        assertEquals(listOf("remote"), breeds.map { it.name })
        assertEquals(listOf("mini"), breeds.single().subBreeds)
        assertEquals("https://images.example.test/remote.jpg", breeds.single().imageUrl)
        assertTrue("cached" !in dao.currentBreedNames)
        assertEquals(1, dao.deleteAllBreedsCalls)
        assertEquals(1, dao.deleteAllSubBreedsCalls)
    }
}

private class FakeBreedsApi(
    private val response: AllBreedsResponse,
) : BreedsApi {
    var getAllCalls: Int = 0
        private set

    override suspend fun getAll(): AllBreedsResponse {
        getAllCalls += 1
        return response
    }

    override suspend fun getRandomImageForBreed(breedName: String): BreedImageResponse {
        return BreedImageResponse(
            message = "https://images.example.test/$breedName.jpg",
            status = "success",
        )
    }

    override suspend fun getRandomImageForSubBreed(
        breedName: String,
        subBreedName: String,
    ): BreedImageResponse {
        return BreedImageResponse(
            message = "https://images.example.test/$breedName-$subBreedName.jpg",
            status = "success",
        )
    }
}

private class FakeBreedsDao(
    initialBreeds: List<BreedEntity> = emptyList(),
    initialSubBreeds: List<SubBreedEntity> = emptyList(),
) : BreedsDao {
    private val breeds = initialBreeds.associateByTo(linkedMapOf()) { it.breedName }
    private val subBreeds = initialSubBreeds.toMutableList()
    private val favorites = mutableListOf<FavoriteEntity>()

    var deleteAllBreedsCalls: Int = 0
        private set
    var deleteAllSubBreedsCalls: Int = 0
        private set

    val currentBreedNames: Set<String>
        get() = breeds.keys

    override suspend fun insertBreeds(breeds: List<BreedEntity>) {
        breeds.forEach { breed -> this.breeds[breed.breedName] = breed }
    }

    override suspend fun insertSubBreeds(subBreeds: List<SubBreedEntity>) {
        this.subBreeds += subBreeds
    }

    override suspend fun deleteAllSubBreeds() {
        deleteAllSubBreedsCalls += 1
        subBreeds.clear()
    }

    override suspend fun deleteAllBreeds() {
        deleteAllBreedsCalls += 1
        breeds.clear()
    }

    override suspend fun getAllBreedsWithSubBreeds(): List<BreedWithSubBreeds> {
        return breeds.values.map { breed ->
            BreedWithSubBreeds(
                breed = breed,
                subBreeds = subBreeds.filter { it.parentBreedName == breed.breedName },
            )
        }
    }

    override suspend fun getBreedByName(name: String): BreedWithSubBreeds? {
        return getAllBreedsWithSubBreeds().firstOrNull { it.breed.breedName.contains(name) }
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        favorites += favorite
    }

    override suspend fun deleteFavoriteByName(breedName: String, subBreedName: String?) {
        favorites.removeAll { favorite ->
            favorite.breedName == breedName && favorite.subBreedName == subBreedName
        }
    }

    override suspend fun getAllFavorites(): List<FavoriteEntity> = favorites.toList()

    override fun observeAllFavorites(): Flow<List<FavoriteEntity>> = flowOf(favorites.toList())

    override suspend fun clearAllFavorites() {
        favorites.clear()
    }
}
