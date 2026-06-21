package ru.bitvibe.waggy.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface BreedsApi {
    @GET("breeds/list/all")
    suspend fun getAll(): AllBreedsResponse

    @GET("breed/{breed}/images/random")
    suspend fun getRandomImageForBreed(
        @Path("breed") breedName: String
    ): BreedImageResponse

    @GET("breed/{breed}/{subBreed}/images/random")
    suspend fun getRandomImageForSubBreed(
        @Path("breed") breedName: String,
        @Path("subBreed") subBreedName: String
    ): BreedImageResponse
}
