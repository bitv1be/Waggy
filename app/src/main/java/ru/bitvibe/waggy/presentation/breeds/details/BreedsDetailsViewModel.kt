package ru.bitvibe.waggy.presentation.breeds.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.bitvibe.waggy.domain.usecase.GetAllFavoritesUseCase
import ru.bitvibe.waggy.domain.usecase.GetBreedByNameUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedFavoriteUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedParams
import ru.bitvibe.waggy.domain.usecase.UseCase
import javax.inject.Inject

@HiltViewModel
class BreedsDetailsViewModel @Inject constructor(
    private val getBreedByNameUseCase: GetBreedByNameUseCase,
    private val getAllFavoritesUseCase: GetAllFavoritesUseCase,
    private val toggleBreedFavoriteUseCase: ToggleBreedFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private companion object {
        const val TAG = "BreedsDetailsViewModel"
    }

    private val _state = MutableStateFlow(BreedsDetailsUiState())
    val state = _state.asStateFlow()

    private val breedsDetailsDest = savedStateHandle.toRoute<BreedsDetailsDestination>()

    init {
        loadBreed()
    }

    fun onEvent(event: BreedsDetailsEvent) {
        when (event) {
            is BreedsDetailsEvent.OnRefresh -> {}
            is BreedsDetailsEvent.OnToggleBreedFavorite -> toggleBreedFavorite(event.name)
            is BreedsDetailsEvent.OnToggleSubBreedFavorite -> toggleSubBreedFavorite(event.name, event.subName)
        }
    }

    private fun toggleBreedFavorite(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFavorite = _state.value.favorites.any { it.breedName == name && it.subBreedName == null }
            toggleBreedFavoriteUseCase(ToggleBreedParams(name = name, subName = null, isFavorite = !isFavorite))
            
            val newFavorites = getAllFavoritesUseCase(UseCase.None)
            _state.update { it.copy(favorites = newFavorites) }
        }
    }

    private fun toggleSubBreedFavorite(name: String, subName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFavorite = _state.value.favorites.any { it.breedName == name && it.subBreedName == subName }
            toggleBreedFavoriteUseCase(ToggleBreedParams(name = name, subName = subName, isFavorite = !isFavorite))
            
            val newFavorites = getAllFavoritesUseCase(UseCase.None)
            _state.update { it.copy(favorites = newFavorites) }
        }
    }

    private fun loadBreed() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            try {
                val breed = getBreedByNameUseCase(breedsDetailsDest.name)
                val favorites = getAllFavoritesUseCase(UseCase.None)
                _state.update {
                    it.copy(
                        isLoading = false,
                        breed = breed,
                        favorites = favorites
                    )
                }
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                Log.e(TAG, message)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = message
                    )
                }
            }
        }
    }
}