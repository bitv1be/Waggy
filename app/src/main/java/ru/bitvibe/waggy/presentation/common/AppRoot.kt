package ru.bitvibe.waggy.presentation.common

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(tween(SCREEN_TRANSITION_MILLIS)) +
                    slideInHorizontally(tween(SCREEN_TRANSITION_MILLIS)) { it / 12 }
            },
            exitTransition = {
                fadeOut(tween(SCREEN_TRANSITION_MILLIS)) +
                    slideOutHorizontally(tween(SCREEN_TRANSITION_MILLIS)) { -it / 12 }
            },
            popEnterTransition = {
                fadeIn(tween(SCREEN_TRANSITION_MILLIS)) +
                    slideInHorizontally(tween(SCREEN_TRANSITION_MILLIS)) { -it / 12 }
            },
            popExitTransition = {
                fadeOut(tween(SCREEN_TRANSITION_MILLIS)) +
                    slideOutHorizontally(tween(SCREEN_TRANSITION_MILLIS)) { it / 12 }
            },
        ) {
            homeScreen { name, recommendationReason ->
                navController.navigateToBreedsDetailsScreen(name, recommendationReason)
            }
            breedsDetailsScreen {
                navController.navigateUp()
            }
        }
    }
}

private const val SCREEN_TRANSITION_MILLIS = 220
