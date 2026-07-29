package ru.bitvibe.waggy.presentation.settings.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.models.Favorite

@Composable
fun FavoriteItemCard(
    favorite: Favorite,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val breedName = favorite.breedName.replaceFirstChar {
        it.titlecase(LocalLocale.current.platformLocale)
    }
    val subBreedName = favorite.subBreedName?.replaceFirstChar {
        it.titlecase(LocalLocale.current.platformLocale)
    }
    val favoriteName = listOfNotNull(breedName, subBreedName).joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = breedName,
                style = MaterialTheme.typography.titleMedium,
            )
            if (subBreedName != null) {
                Text(
                    text = subBreedName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.remove_favorite, favoriteName),
            )
        }
    }
}
