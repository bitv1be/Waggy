package ru.bitvibe.waggy.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AllBreedsResponse(
    val message: Map<String, List<String>>,
    val status: String,
)
