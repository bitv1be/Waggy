package ru.bitvibe.waggy.domain.models

data class Breed(
    val name: String,
    val imageUrl: String?,
    val subBreeds: List<String>
)