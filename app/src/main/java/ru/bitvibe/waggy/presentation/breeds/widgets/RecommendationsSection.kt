package ru.bitvibe.waggy.presentation.breeds.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.models.BreedRecommendation
import ru.bitvibe.waggy.domain.models.RecommendationReason
import ru.bitvibe.waggy.domain.models.RecommendationSource
import ru.bitvibe.waggy.presentation.breeds.RecommendationsUiState

@Composable
fun RecommendationsSection(
    state: RecommendationsUiState,
    onRecommendationClick: (String, String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state) {
            is RecommendationsUiState.Content -> {
                val isPopular = state.result.source == RecommendationSource.POPULAR
                RecommendationHeader(isPopular = isPopular)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.result.recommendations,
                        key = { recommendation -> recommendation.breed.name },
                    ) { recommendation ->
                        RecommendationCard(
                            recommendation = recommendation,
                            isPopular = isPopular,
                            onClick = {
                                onRecommendationClick(
                                    recommendation.breed.name,
                                    recommendation.reason.toExplanation(),
                                )
                            },
                        )
                    }
                }
            }

            RecommendationsUiState.Loading -> {
                RecommendationHeader(isPopular = false)
                RecommendationStatusCard {
                    val loadingDescription = stringResource(R.string.loading_recommendations)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(28.dp)
                            .semantics { contentDescription = loadingDescription },
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = stringResource(R.string.loading_recommendations),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            RecommendationsUiState.Empty -> {
                RecommendationHeader(isPopular = false)
                RecommendationStatusCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.recommendations_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.recommendations_empty_message),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            is RecommendationsUiState.Error -> {
                RecommendationHeader(isPopular = false)
                RecommendationStatusCard {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.recommendations_error_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.recommendations_error_message),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationHeader(isPopular: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(
                if (isPopular) R.string.popular_breeds else R.string.recommendations_for_you,
            ),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(
                if (isPopular) {
                    R.string.popular_breeds_description
                } else {
                    R.string.recommendations_based_on_favorites
                },
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun RecommendationStatusCard(content: @Composable RowScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun RecommendationCard(
    recommendation: BreedRecommendation,
    isPopular: Boolean,
    onClick: () -> Unit,
) {
    BreedItem(
        breed = recommendation.breed,
        onClick = onClick,
        modifier = Modifier.width(RECOMMENDATION_CARD_WIDTH),
        supportingText = recommendation.reason.toExplanation(),
        badgeText = if (isPopular) stringResource(R.string.popular_breed_badge) else null,
    )
}

private fun RecommendationReason.toExplanation(): String {
    return when (this) {
        is RecommendationReason.AiGenerated -> explanation
    }
}

private val RECOMMENDATION_CARD_WIDTH = 280.dp
