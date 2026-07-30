package ru.bitvibe.waggy.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

fun NavGraphBuilder.homeScreen(onNavigateToBreedsDetails: (String, String?) -> Unit) {
    composable<HomeDestination> {
        HomeScreen(onNavigateToBreedsDetails)
    }
}

fun NavController.navigateToHomeScreen() {
    navigate(HomeDestination)
}
