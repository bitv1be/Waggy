package ru.bitvibe.waggy.presentation.breeds.details

import android.graphics.Bitmap
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite

data class BreedsDetailsUiState(
    val isLoading: Boolean = false,
    val breed: Breed? = null,
    val favorites: List<Favorite> = emptyList(),
    val recommendationReason: String? = null,
    val description: BreedDescriptionUiState = BreedDescriptionUiState.Loading,
    val error: String? = null,
    val isSegmenting: Boolean = false,
    val foregroundBitmap: Bitmap? = null,
    val dominantColorArgb: Int? = null,
)

sealed interface BreedDescriptionUiState {
    data object Loading : BreedDescriptionUiState

    data class Content(
        val description: String,
    ) : BreedDescriptionUiState

    data object Error : BreedDescriptionUiState
}

sealed interface BreedsDetailsEvent {
    data class OnRefresh(val forced: Boolean = false) : BreedsDetailsEvent
    data object OnRetryDescription : BreedsDetailsEvent
    data class OnToggleBreedFavorite(val name: String) : BreedsDetailsEvent
    data class OnToggleSubBreedFavorite(val name: String, val subName: String) : BreedsDetailsEvent
}
