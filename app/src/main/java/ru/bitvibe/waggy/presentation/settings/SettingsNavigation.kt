package ru.bitvibe.waggy.presentation.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SettingsDestination

fun NavGraphBuilder.settingsScreen() {
    composable<SettingsDestination> {
        SettingsScreen()
    }
}

fun NavController.navigateToSettingsScreen() {
    navigate(SettingsDestination)
}