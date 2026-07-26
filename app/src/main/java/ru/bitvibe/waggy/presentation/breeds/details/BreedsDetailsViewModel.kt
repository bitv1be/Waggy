package ru.bitvibe.waggy.presentation.breeds.details

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.bitvibe.waggy.domain.usecase.GetAllFavoritesUseCase
import ru.bitvibe.waggy.domain.usecase.GetBreedByNameUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedFavoriteUseCase
import ru.bitvibe.waggy.domain.usecase.ToggleBreedParams
import ru.bitvibe.waggy.domain.usecase.UseCase
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class BreedsDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getBreedByNameUseCase: GetBreedByNameUseCase,
    private val getAllFavoritesUseCase: GetAllFavoritesUseCase,
    private val toggleBreedFavoriteUseCase: ToggleBreedFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private companion object {
        const val TAG = "BreedsDetailsViewModel"
    }

    private val segmenterOptions = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .build()

    private val _state = MutableStateFlow(BreedsDetailsUiState())
    val state = _state.asStateFlow()

    private val breedsDetailsDest = savedStateHandle.toRoute<BreedsDetailsDestination>()

    init {
        loadBreed()
    }

    fun onEvent(event: BreedsDetailsEvent) {
        when (event) {
            is BreedsDetailsEvent.OnRefresh -> {
                _state.value.foregroundBitmap?.recycle()
                _state.update { it.copy(foregroundBitmap = null) }
                loadBreed()
            }

            is BreedsDetailsEvent.OnToggleBreedFavorite -> toggleBreedFavorite(event.name)
            is BreedsDetailsEvent.OnToggleSubBreedFavorite -> toggleSubBreedFavorite(
                event.name,
                event.subName
            )
        }
    }

    private fun toggleBreedFavorite(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFavorite =
                _state.value.favorites.any { it.breedName == name && it.subBreedName == null }
            toggleBreedFavoriteUseCase(
                ToggleBreedParams(
                    name = name,
                    subName = null,
                    isFavorite = !isFavorite
                )
            )

            val newFavorites = getAllFavoritesUseCase(UseCase.None)
            _state.update { it.copy(favorites = newFavorites) }
        }
    }

    private fun toggleSubBreedFavorite(name: String, subName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFavorite =
                _state.value.favorites.any { it.breedName == name && it.subBreedName == subName }
            toggleBreedFavoriteUseCase(
                ToggleBreedParams(
                    name = name,
                    subName = subName,
                    isFavorite = !isFavorite
                )
            )

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
                if (breed?.imageUrl != null) {
                    loadAndSegmentImage(breed.imageUrl)
                }
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                Log.e(TAG, message)
                Firebase.crashlytics.log(message)
                Firebase.crashlytics.recordException(e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = message
                    )
                }
            }
        }
    }

    private fun loadAndSegmentImage(imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isSegmenting = true) }
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.image.toBitmap()
                    val foreground = segmentDog(bitmap)
                    _state.update {
                        it.copy(
                            isSegmenting = false,
                            foregroundBitmap = foreground
                        )
                    }
                } else {
                    _state.update { it.copy(isSegmenting = false) }
                }
            } catch (e: Exception) {
                val message = "Error downloading/segmenting image"
                Log.e(TAG, message, e)
                Firebase.crashlytics.log(message)
                Firebase.crashlytics.recordException(e)
                _state.update { it.copy(isSegmenting = false) }
            }
        }
    }

    private suspend fun segmentDog(bitmap: Bitmap): Bitmap? = suspendCancellableCoroutine { cont ->
        val segmenter = SubjectSegmentation.getClient(segmenterOptions)
        cont.invokeOnCancellation { segmenter.close() }

        segmenter.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                if (cont.isActive) cont.resume(result.foregroundBitmap)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Segmentation failed", error)
                Firebase.crashlytics.log("Segmentation failed")
                Firebase.crashlytics.recordException(error)
                if (cont.isActive) cont.resume(null)
            }
            .addOnCompleteListener {
                segmenter.close()
            }
    }

    override fun onCleared() {
        _state.value.foregroundBitmap?.recycle()
    }
}
