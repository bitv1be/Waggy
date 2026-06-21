package ru.bitvibe.waggy.presentation.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ru.bitvibe.waggy.presentation.HomeDestination
import ru.bitvibe.waggy.presentation.breeds.details.breedsDetailsScreen
import ru.bitvibe.waggy.presentation.breeds.details.navigateToBreedsDetailsScreen
import ru.bitvibe.waggy.presentation.homeScreen

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        homeScreen { name ->
            navController.navigateToBreedsDetailsScreen(name)
        }
        breedsDetailsScreen {
            navController.navigateUp()
        }
    }
}