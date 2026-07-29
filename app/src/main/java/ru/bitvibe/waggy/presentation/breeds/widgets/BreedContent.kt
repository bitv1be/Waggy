package ru.bitvibe.waggy.presentation.breeds.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.domain.models.Breed

@Composable
fun BreedContent(
    breeds: List<Breed>,
    onNavigateToBreedsDetails: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = breeds,
            key = { it.name },
        ) { breed ->
            BreedItem(
                breed = breed,
                onClick = { onNavigateToBreedsDetails(breed.name) },
            )
        }
    }
}
