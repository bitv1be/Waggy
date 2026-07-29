package ru.bitvibe.waggy.presentation.breeds

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.presentation.breeds.widgets.BreedContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedsScreen(
    onNavigateToBreedsDetails: (String) -> Unit,
    viewModel: BreedsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.breeds.isNotEmpty()) {
        if (state.error != null && state.breeds.isNotEmpty()) {
            snackbarHostState.showSnackbar(state.error.orEmpty())
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.breeds_title)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(BreedsEvent.OnRefresh(true)) },
        ) {
            val contentState = when {
                state.breeds.isNotEmpty() -> BreedsContentState.CONTENT
                state.isLoading -> BreedsContentState.LOADING
                state.error != null -> BreedsContentState.ERROR
                else -> BreedsContentState.EMPTY
            }
            AnimatedContent(
                targetState = contentState,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "breed_content_state",
            ) { targetState ->
                when (targetState) {
                    BreedsContentState.CONTENT -> {
                        BreedContent(
                            breeds = state.breeds,
                            onNavigateToBreedsDetails = onNavigateToBreedsDetails,
                        )
                    }

                    BreedsContentState.LOADING -> {
                        LoadingBreeds()
                    }

                    BreedsContentState.ERROR -> {
                        BreedsMessage(
                            title = state.error.orEmpty(),
                            message = stringResource(R.string.breeds_empty_message),
                            onRetry = { viewModel.onEvent(BreedsEvent.OnRefresh(true)) },
                        )
                    }

                    BreedsContentState.EMPTY -> {
                        BreedsMessage(
                            title = stringResource(R.string.breeds_empty_title),
                            message = stringResource(R.string.breeds_empty_message),
                            onRetry = { viewModel.onEvent(BreedsEvent.OnRefresh(true)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingBreeds() {
    val loadingDescription = stringResource(R.string.loading_breeds)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = loadingDescription },
        )
    }
}

@Composable
private fun BreedsMessage(
    title: String,
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

private enum class BreedsContentState {
    LOADING,
    CONTENT,
    ERROR,
    EMPTY,
}
