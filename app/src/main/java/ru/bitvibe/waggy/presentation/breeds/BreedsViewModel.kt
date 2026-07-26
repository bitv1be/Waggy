package ru.bitvibe.waggy.presentation.breeds

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.bitvibe.waggy.domain.usecase.GetAllBreedsUseCase
import ru.bitvibe.waggy.domain.usecase.UseCase
import javax.inject.Inject

@HiltViewModel
class BreedsViewModel @Inject constructor(
    private val getAllBreedsUseCase: GetAllBreedsUseCase
) : ViewModel() {
    private companion object {
        const val TAG = "BreedsViewModel"
    }

    private val _state = MutableStateFlow(BreedsUiState())
    val state = _state.asStateFlow()

    init {
        loadBreeds()
    }

    fun onEvent(event: BreedsEvent) {
        when (event) {
            is BreedsEvent.OnRefresh -> loadBreeds(event.forced)
        }
    }

    private fun loadBreeds(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val result = getAllBreedsUseCase(UseCase.None)
                _state.update {
                    it.copy(
                        isLoading = false,
                        breeds = result
                    )
                }
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                Firebase.crashlytics.log(message)
                Firebase.crashlytics.recordException(e)
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