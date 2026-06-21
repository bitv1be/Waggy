package ru.bitvibe.waggy.presentation.breeds.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.bitvibe.waggy.domain.models.Breed

@Composable
fun BreedContent(
    breeds: List<Breed>,
    onNavigateToBreedsDetails: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(breeds) {
            BreedItem(it, modifier = Modifier.clickable {
                onNavigateToBreedsDetails(it.name)
            })
        }
    }
}