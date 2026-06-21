package ru.bitvibe.waggy.presentation.breeds

import ru.bitvibe.waggy.domain.models.Breed

data class BreedsUiState(
    val isLoading: Boolean = false,
    val breeds: List<Breed> = emptyList(),
    val error: String? = null
)

sealed interface BreedsEvent {
    data class OnRefresh(val forced: Boolean = false) : BreedsEvent
}