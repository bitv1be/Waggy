package ru.bitvibe.waggy.presentation.breeds.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.presentation.breeds.RecommendationsUiState

@Composable
fun BreedContent(
    breeds: List<Breed>,
    recommendations: RecommendationsUiState,
    onNavigateToBreedsDetails: (String, String?) -> Unit,
    onRetryRecommendations: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(
            key = "recommendations",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            RecommendationsSection(
                state = recommendations,
                onRecommendationClick = { name, recommendationReason ->
                    onNavigateToBreedsDetails(name, recommendationReason)
                },
                onRetry = onRetryRecommendations,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item(
            key = "all_breeds_header",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Text(
                text = stringResource(R.string.all_breeds),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        items(
            items = breeds,
            key = { it.name },
        ) { breed ->
            BreedItem(
                breed = breed,
                onClick = { onNavigateToBreedsDetails(breed.name, null) },
            )
        }
    }
}
