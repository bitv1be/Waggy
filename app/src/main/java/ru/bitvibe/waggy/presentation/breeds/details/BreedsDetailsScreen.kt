package ru.bitvibe.waggy.presentation.breeds.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.presentation.breeds.details.widgets.BreedHeaderWidget
import ru.bitvibe.waggy.presentation.breeds.details.widgets.SubBreedItemWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedsDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BreedsDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breeds details") },
                navigationIcon = {
                    IconButton(onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Go Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(BreedsDetailsEvent.OnRefresh(true)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.error != null && state.breed == null -> {
                    Button(
                        onClick = { viewModel.onEvent(BreedsDetailsEvent.OnRefresh(true)) }
                    ) {
                        Text(state.error!!)
                    }
                }

                state.breed != null && state.error == null -> {
                    BreedsDetailsContent(
                        breed = state.breed!!,
                        favorites = state.favorites,
                        foregroundBitmap = state.foregroundBitmap,
                        onToggleBreedFavorite = { name -> viewModel.onEvent(BreedsDetailsEvent.OnToggleBreedFavorite(name)) },
                        onToggleSubBreedFavorite = { name, subName ->
                            viewModel.onEvent(
                                BreedsDetailsEvent.OnToggleSubBreedFavorite(
                                    name,
                                    subName
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BreedsDetailsContent(
    breed: Breed,
    favorites: List<Favorite>,
    foregroundBitmap: android.graphics.Bitmap?,
    onToggleBreedFavorite: (String) -> Unit,
    onToggleSubBreedFavorite: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BreedHeaderWidget(
                breed = breed,
                isFavorite = favorites.any { it.breedName.equals(breed.name, ignoreCase = true) && it.subBreedName == null },
                onToggleFavorite = { onToggleBreedFavorite(breed.name) },
                foregroundBitmap = foregroundBitmap
            )
        }

        item {
            Text(
                text = breed.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (breed.subBreeds.isNotEmpty()) {
            item {
                Text(
                    text = "Sub-breeds",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }

            items(breed.subBreeds) { subBreed ->
                SubBreedItemWidget(
                    subBreed = subBreed,
                    isFavorite = favorites.any { it.breedName.equals(breed.name, ignoreCase = true) && it.subBreedName.equals(subBreed, ignoreCase = true) },
                    onToggleFavorite = { onToggleSubBreedFavorite(breed.name, subBreed) }
                )
            }
        }
    }
}
