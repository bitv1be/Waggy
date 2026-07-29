package ru.bitvibe.waggy.presentation.common

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.presentation.breeds.details.BreedsDetailsContent
import ru.bitvibe.waggy.presentation.breeds.widgets.BreedContent

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showBackground = true)
private annotation class FormFactorPreviews

@FormFactorPreviews
@Composable
private fun BreedGridPreview() {
    WaggyTheme {
        BreedContent(
            breeds = previewBreeds,
            onNavigateToBreedsDetails = {},
        )
    }
}

@Preview(
    name = "Details dark",
    device = Devices.PHONE,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Details tablet", device = Devices.TABLET, showBackground = true)
@Composable
private fun BreedDetailsPreview() {
    WaggyTheme {
        BreedsDetailsContent(
            breed = previewBreeds.first(),
            favorites = emptyList(),
            foregroundBitmap = null,
            dominantColorArgb = 0xFF8A5A3C.toInt(),
            onToggleBreedFavorite = {},
            onToggleSubBreedFavorite = { _, _ -> },
        )
    }
}

private val previewBreeds = listOf(
    Breed(
        name = "retriever",
        imageUrl = null,
        subBreeds = listOf("golden", "chesapeake"),
    ),
    Breed(
        name = "hound",
        imageUrl = null,
        subBreeds = listOf("afghan"),
    ),
    Breed(
        name = "terrier",
        imageUrl = null,
        subBreeds = emptyList(),
    ),
)
