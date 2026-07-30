package ru.bitvibe.waggy.presentation.breeds.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.presentation.common.photoPalette
import ru.bitvibe.waggy.presentation.common.rememberDominantPhotoColor

@Composable
fun BreedItem(
    breed: Breed,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    badgeText: String? = null,
) {
    val dominantPhotoColor = rememberDominantPhotoColor(breed.imageUrl)
    val targetPalette = photoPalette(dominantPhotoColor.argb)
    val containerColor by animateColorAsState(
        targetValue = targetPalette.container,
        animationSpec = tween(PALETTE_TRANSITION_MILLIS),
        label = "breed_card_container",
    )
    val contentColor by animateColorAsState(
        targetValue = targetPalette.content,
        animationSpec = tween(PALETTE_TRANSITION_MILLIS),
        label = "breed_card_content",
    )
    val accentColor by animateColorAsState(
        targetValue = targetPalette.accent,
        animationSpec = tween(PALETTE_TRANSITION_MILLIS),
        label = "breed_card_accent",
    )
    val onAccentColor by animateColorAsState(
        targetValue = targetPalette.onAccent,
        animationSpec = tween(PALETTE_TRANSITION_MILLIS),
        label = "breed_card_on_accent",
    )
    var isImageLoading by remember(breed.imageUrl) { mutableStateOf(breed.imageUrl != null) }
    val displayName = breed.name.replaceFirstChar {
        it.titlecase(LocalLocale.current.platformLocale)
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(PHOTO_ASPECT_RATIO)
                    .background(containerColor),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(breed.imageUrl)
                        .allowHardware(false)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.breed_photo_description, displayName),
                    modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_ASPECT_RATIO),
                    contentScale = ContentScale.Crop,
                    onLoading = { isImageLoading = true },
                    onSuccess = { state ->
                        isImageLoading = false
                        dominantPhotoColor.onImageLoaded(state.result.image)
                    },
                    onError = { isImageLoading = false },
                )
                if (isImageLoading) {
                    val loadingDescription = stringResource(R.string.loading_breeds)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(28.dp)
                            .semantics { contentDescription = loadingDescription },
                        color = accentColor,
                        strokeWidth = 2.dp,
                    )
                }
                badgeText?.let { text ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = MaterialTheme.shapes.small,
                        color = accentColor,
                        contentColor = onAccentColor,
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                    )
                    supportingText?.let { text ->
                        Text(
                            text = text,
                            minLines = 2,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = SUPPORTING_TEXT_ALPHA),
                        )
                    }
                }
                if (breed.subBreeds.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = accentColor,
                        contentColor = onAccentColor,
                    ) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.sub_breed_count,
                                breed.subBreeds.size,
                                breed.subBreeds.size,
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

private const val PALETTE_TRANSITION_MILLIS = 420
private const val PHOTO_ASPECT_RATIO = 4f / 3f
private const val SUPPORTING_TEXT_ALPHA = 0.82f
