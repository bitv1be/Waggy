package ru.bitvibe.waggy.presentation.breeds.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.models.Breed
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.presentation.breeds.details.widgets.BreedHeaderWidget
import ru.bitvibe.waggy.presentation.breeds.details.widgets.SubBreedItemWidget
import ru.bitvibe.waggy.presentation.common.PhotoPalette
import ru.bitvibe.waggy.presentation.common.photoPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedsDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BreedsDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val screenTitle = state.breed?.name?.replaceFirstChar {
        it.titlecase(LocalLocale.current.platformLocale)
    } ?: stringResource(R.string.breed_details_title)

    LaunchedEffect(state.error, state.breed != null) {
        if (state.error != null && state.breed != null) {
            snackbarHostState.showSnackbar(state.error.orEmpty())
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(BreedsDetailsEvent.OnRefresh(true)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            val contentState = when {
                state.breed != null -> DetailsContentState.CONTENT
                state.isLoading -> DetailsContentState.LOADING
                else -> DetailsContentState.ERROR
            }
            AnimatedContent(
                targetState = contentState,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "breed_details_state",
            ) { targetState ->
                when (targetState) {
                    DetailsContentState.LOADING -> DetailsLoading()
                    DetailsContentState.ERROR -> DetailsError(
                        message = state.error ?: stringResource(R.string.unknown_error),
                        onRetry = { viewModel.onEvent(BreedsDetailsEvent.OnRefresh(true)) },
                    )

                    DetailsContentState.CONTENT -> {
                        BreedsDetailsContent(
                            breed = requireNotNull(state.breed),
                            favorites = state.favorites,
                            foregroundBitmap = state.foregroundBitmap,
                            dominantColorArgb = state.dominantColorArgb,
                            recommendationReason = state.recommendationReason,
                            descriptionState = state.description,
                            onRetryDescription = {
                                viewModel.onEvent(BreedsDetailsEvent.OnRetryDescription)
                            },
                            onToggleBreedFavorite = { name ->
                                viewModel.onEvent(BreedsDetailsEvent.OnToggleBreedFavorite(name))
                            },
                            onToggleSubBreedFavorite = { name, subName ->
                                viewModel.onEvent(
                                    BreedsDetailsEvent.OnToggleSubBreedFavorite(name, subName),
                                )
                            },
                        )
                    }
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
    dominantColorArgb: Int?,
    onToggleBreedFavorite: (String) -> Unit,
    onToggleSubBreedFavorite: (String, String) -> Unit,
    recommendationReason: String? = null,
    descriptionState: BreedDescriptionUiState = BreedDescriptionUiState.Loading,
    onRetryDescription: () -> Unit = {},
) {
    val targetPalette = photoPalette(dominantColorArgb)
    val palette = PhotoPalette(
        container = animateColorAsState(
            targetValue = targetPalette.container,
            animationSpec = tween(PALETTE_TRANSITION_MILLIS),
            label = "details_photo_container",
        ).value,
        content = animateColorAsState(
            targetValue = targetPalette.content,
            animationSpec = tween(PALETTE_TRANSITION_MILLIS),
            label = "details_photo_content",
        ).value,
        accent = animateColorAsState(
            targetValue = targetPalette.accent,
            animationSpec = tween(PALETTE_TRANSITION_MILLIS),
            label = "details_photo_accent",
        ).value,
        onAccent = animateColorAsState(
            targetValue = targetPalette.onAccent,
            animationSpec = tween(PALETTE_TRANSITION_MILLIS),
            label = "details_photo_on_accent",
        ).value,
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useTwoPaneLayout = maxWidth >= DETAILS_TWO_PANE_BREAKPOINT
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (useTwoPaneLayout) {
                Row(
                    modifier = Modifier
                        .widthIn(max = DETAILS_MAX_WIDTH)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    BreedHeaderWidget(
                        breed = breed,
                        isFavorite = favorites.isBreedFavorite(breed.name),
                        onToggleFavorite = { onToggleBreedFavorite(breed.name) },
                        foregroundBitmap = foregroundBitmap,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                    )
                    BreedInformation(
                        breed = breed,
                        favorites = favorites,
                        palette = palette,
                        onToggleSubBreedFavorite = onToggleSubBreedFavorite,
                        recommendationReason = recommendationReason,
                        descriptionState = descriptionState,
                        onRetryDescription = onRetryDescription,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = DETAILS_COMPACT_MAX_WIDTH)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    BreedHeaderWidget(
                        breed = breed,
                        isFavorite = favorites.isBreedFavorite(breed.name),
                        onToggleFavorite = { onToggleBreedFavorite(breed.name) },
                        foregroundBitmap = foregroundBitmap,
                        palette = palette,
                    )
                    BreedInformation(
                        breed = breed,
                        favorites = favorites,
                        palette = palette,
                        onToggleSubBreedFavorite = onToggleSubBreedFavorite,
                        recommendationReason = recommendationReason,
                        descriptionState = descriptionState,
                        onRetryDescription = onRetryDescription,
                    )
                }
            }
        }
    }
}

@Composable
private fun BreedInformation(
    breed: Breed,
    favorites: List<Favorite>,
    palette: PhotoPalette,
    onToggleSubBreedFavorite: (String, String) -> Unit,
    recommendationReason: String?,
    descriptionState: BreedDescriptionUiState,
    onRetryDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = breed.name.replaceFirstChar {
                it.titlecase(LocalLocale.current.platformLocale)
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        AiBreedGuideCard(
            recommendationReason = recommendationReason,
            descriptionState = descriptionState,
            palette = palette,
            onRetry = onRetryDescription,
        )

        if (breed.subBreeds.isNotEmpty()) {
            Text(
                text = stringResource(R.string.sub_breeds_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            breed.subBreeds.forEach { subBreed ->
                SubBreedItemWidget(
                    subBreed = subBreed,
                    isFavorite = favorites.isSubBreedFavorite(breed.name, subBreed),
                    onToggleFavorite = { onToggleSubBreedFavorite(breed.name, subBreed) },
                    palette = palette,
                )
            }
        }
    }
}

@Composable
private fun AiBreedGuideCard(
    recommendationReason: String?,
    descriptionState: BreedDescriptionUiState,
    palette: PhotoPalette,
    onRetry: () -> Unit,
) {
    val shape = MaterialTheme.shapes.extraLarge
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            palette.accent.copy(alpha = 0.78f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.52f),
            palette.accent.copy(alpha = 0.30f),
        ),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(borderGradient)
            .padding(1.dp),
    ) {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = palette.accent,
                        contentColor = palette.onAccent,
                    ) {
                        Text(
                            text = stringResource(R.string.ai_badge),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.ai_breed_guide),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.ai_breed_guide_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                recommendationReason
                    ?.trim()
                    ?.takeIf { reason -> reason.isNotEmpty() }
                    ?.let { reason ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = palette.accent.copy(alpha = 0.10f),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.ai_recommendation_reason),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.accent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        HorizontalDivider(color = palette.accent.copy(alpha = 0.18f))
                    }

                AnimatedContent(
                    targetState = descriptionState,
                    label = "ai_breed_description_state",
                ) { state ->
                    when (state) {
                        BreedDescriptionUiState.Loading -> GeneratingDescription()
                        is BreedDescriptionUiState.Content -> {
                            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text(
                                    text = stringResource(R.string.ai_breed_overview),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.accent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = state.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        BreedDescriptionUiState.Error -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(R.string.ai_description_error_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = stringResource(R.string.ai_description_error_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = onRetry) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratingDescription() {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_description_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AI_PULSE_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ai_description_pulse_alpha",
    )
    val loadingDescription = stringResource(R.string.generating_ai_description)
    Column(
        modifier = Modifier.semantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            text = loadingDescription,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.generating_ai_description_details),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.alpha(pulseAlpha),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            DescriptionSkeletonLine(widthFraction = 0.96f)
            DescriptionSkeletonLine(widthFraction = 0.84f)
            DescriptionSkeletonLine(widthFraction = 0.62f)
        }
    }
}

@Composable
private fun DescriptionSkeletonLine(widthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
    )
}

@Composable
private fun DetailsLoading() {
    val description = stringResource(R.string.loading_breeds)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = description },
        )
    }
}

@Composable
private fun DetailsError(message: String, onRetry: () -> Unit) {
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
                text = message,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

private fun List<Favorite>.isBreedFavorite(breedName: String): Boolean {
    return any { favorite ->
        favorite.breedName.equals(breedName, ignoreCase = true) && favorite.subBreedName == null
    }
}

private fun List<Favorite>.isSubBreedFavorite(breedName: String, subBreedName: String): Boolean {
    return any { favorite ->
        favorite.breedName.equals(breedName, ignoreCase = true) &&
            favorite.subBreedName.equals(subBreedName, ignoreCase = true)
    }
}

private enum class DetailsContentState {
    LOADING,
    CONTENT,
    ERROR,
}

private val DETAILS_TWO_PANE_BREAKPOINT = 840.dp
private val DETAILS_MAX_WIDTH = 1200.dp
private val DETAILS_COMPACT_MAX_WIDTH = 720.dp
private const val PALETTE_TRANSITION_MILLIS = 420
private const val AI_PULSE_MILLIS = 900
