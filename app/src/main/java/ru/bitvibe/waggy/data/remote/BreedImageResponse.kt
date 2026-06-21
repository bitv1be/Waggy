package ru.bitvibe.waggy.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BreedImageResponse(
    val message: String,
    val status: String
)