package ru.bitvibe.waggy.presentation.breeds.details

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data class BreedsDetailsDestination(
    val name: String,
    val recommendationReason: String? = null,
)

fun NavGraphBuilder.breedsDetailsScreen(onNavigateBack: () -> Unit) {
    composable<BreedsDetailsDestination> {
        BreedsDetailsScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavController.navigateToBreedsDetailsScreen(
    name: String,
    recommendationReason: String? = null,
) {
    navigate(BreedsDetailsDestination(name, recommendationReason))
}
