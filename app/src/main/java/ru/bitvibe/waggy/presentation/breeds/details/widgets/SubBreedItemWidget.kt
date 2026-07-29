package ru.bitvibe.waggy.presentation.breeds.details.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.presentation.common.PhotoPalette

@Composable
fun SubBreedItemWidget(
    subBreed: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    palette: PhotoPalette,
    modifier: Modifier = Modifier,
) {
    val displayName = subBreed.replaceFirstChar {
        it.titlecase(LocalLocale.current.platformLocale)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = palette.container,
        contentColor = palette.content,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
            )
            FilledTonalIconButton(
                onClick = onToggleFavorite,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isFavorite) palette.accent else palette.container,
                    contentColor = if (isFavorite) palette.onAccent else palette.content,
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
}
