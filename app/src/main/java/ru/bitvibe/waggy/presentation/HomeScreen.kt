package ru.bitvibe.waggy.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.bitvibe.waggy.presentation.breeds.BreedsDestination
import ru.bitvibe.waggy.presentation.breeds.breedsScreen
import ru.bitvibe.waggy.presentation.breeds.navigateToBreedsScreen
import ru.bitvibe.waggy.presentation.settings.SettingsDestination
import ru.bitvibe.waggy.presentation.settings.navigateToSettingsScreen
import ru.bitvibe.waggy.presentation.settings.settingsScreen

@Composable
fun HomeScreen(
    onNavigateToBreedsDetails: (String) -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            MainBottomBar(
                navController.currentBackStackEntryAsState().value?.destination?.hierarchy,
                onNavigateToBreeds = { navController.navigateToBreedsScreen() },
                onNavigateToSettings = { navController.navigateToSettingsScreen() }
            )
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            navController = navController,
            startDestination = BreedsDestination
        ) {
            breedsScreen(onNavigateToBreedsDetails)
            settingsScreen()
        }
    }
}

@Composable
private fun MainBottomBar(
    hierarchy: Sequence<NavDestination>?,
    onNavigateToBreeds: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, "Breeds") },
            label = { Text("Breeds") },
            onClick = onNavigateToBreeds,
            selected = hierarchy?.any { it.hasRoute(BreedsDestination::class) } == true
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, "Settings") },
            label = { Text("Settings") },
            onClick = onNavigateToSettings,
            selected = hierarchy?.any { it.hasRoute(SettingsDestination::class) } == true
        )
    }

}