package ru.bitvibe.waggy.presentation.widget

import kotlinx.serialization.Serializable

@Serializable
sealed interface BreedWidgetState {
    @Serializable
    data object Loading : BreedWidgetState

    @Serializable
    data class Loaded(
        val breedName: String,
        val imageUrl: String,
        val subBreedName: String?
    ) : BreedWidgetState

    @Serializable
    data class Error(val message: String) : BreedWidgetState
}