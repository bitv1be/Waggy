package ru.bitvibe.waggy.presentation.breeds.details.widgets

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.presentation.common.PhotoPalette

@Composable
fun BreedHeaderWidget(
    breed: Breed,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    foregroundBitmap: Bitmap?,
    palette: PhotoPalette,
    modifier: Modifier = Modifier,
) {
    val displayName = breed.name.replaceFirstChar {
        it.titlecase(LocalLocale.current.platformLocale)
    }
    val imageShape = MaterialTheme.shapes.extraLarge

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(HEADER_ASPECT_RATIO)
            .clip(imageShape)
            .background(palette.container)
            .border(1.dp, palette.accent.copy(alpha = 0.24f), imageShape),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(breed.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.breed_photo_description, displayName),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.24f),
                        ),
                        startY = 220f,
                    ),
                ),
        )

        if (foregroundBitmap != null) {
            Image(
                bitmap = foregroundBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        FilledTonalIconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = palette.accent,
                contentColor = palette.onAccent,
            ),
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.remove_breed_favorite else R.string.add_breed_favorite,
                    displayName,
                ),
            )
        }
    }
}

private const val HEADER_ASPECT_RATIO = 4f / 3f
