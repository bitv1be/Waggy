package ru.bitvibe.waggy.presentation.breeds

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.bitvibe.waggy.presentation.breeds.widgets.BreedContent

@Composable
fun BreedsScreen(
    onNavigateToBreedsDetails: (String) -> Unit,
    viewModel: BreedsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(BreedsEvent.OnRefresh(true)) }
        ) {
            when {
                state.error != null && state.breeds.isEmpty() -> {
                    Button(
                        onClick = { viewModel.onEvent(BreedsEvent.OnRefresh(true)) }
                    ) {
                        Text(state.error!!)
                    }
                }

                state.breeds.isNotEmpty() && state.error == null -> {
                    BreedContent(state.breeds, onNavigateToBreedsDetails)
                }
            }
        }
    }
}