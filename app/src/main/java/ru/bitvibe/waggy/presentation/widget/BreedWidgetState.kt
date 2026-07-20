package ru.bitvibe.waggy.presentation.widget

import kotlinx.serialization.Serializable

@Serializable
sealed interface BreedWidgetState {
    @Serializable
    data object Loading : BreedWidgetState

    @Serializable
    data class Loaded(
        val breedName: String,
        val foregroundImage: ByteArray?,
        val backgroundImage: ByteArray,
        val subBreedName: String?
    ) : BreedWidgetState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Loaded

            if (breedName != other.breedName) return false
            if (!foregroundImage.contentEquals(other.foregroundImage)) return false
            if (!backgroundImage.contentEquals(other.backgroundImage)) return false
            if (subBreedName != other.subBreedName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = breedName.hashCode()
            result = 31 * result + (foregroundImage?.contentHashCode() ?: 0)
            result = 31 * result + backgroundImage.contentHashCode()
            result = 31 * result + (subBreedName?.hashCode() ?: 0)
            return result
        }
    }

    @Serializable
    data class Error(val message: String) : BreedWidgetState
}