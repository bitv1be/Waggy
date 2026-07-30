package ru.bitvibe.waggy.presentation.breeds

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object BreedsDestination

fun NavGraphBuilder.breedsScreen(onNavigateToBreedsDetails: (String, String?) -> Unit) {
    composable<BreedsDestination> {
        BreedsScreen(onNavigateToBreedsDetails)
    }
}

fun NavController.navigateToBreedsScreen() {
    navigate(BreedsDestination)
}
