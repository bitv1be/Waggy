package ru.bitvibe.waggy.presentation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.presentation.breeds.BreedsDestination
import ru.bitvibe.waggy.presentation.breeds.breedsScreen
import ru.bitvibe.waggy.presentation.settings.SettingsDestination
import ru.bitvibe.waggy.presentation.settings.settingsScreen

@Composable
fun HomeScreen(
    onNavigateToBreedsDetails: (String, String?) -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val hierarchy = backStackEntry?.destination?.hierarchy
    val selectedDestination = when {
        hierarchy.hasRoute<SettingsDestination>() -> MainDestination.SETTINGS
        else -> MainDestination.BREEDS
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= NAVIGATION_RAIL_BREAKPOINT
        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                MainNavigationRail(
                    selectedDestination = selectedDestination,
                    onSelect = navController::navigateToTopLevel,
                )
                HomeNavHost(
                    navController = navController,
                    onNavigateToBreedsDetails = onNavigateToBreedsDetails,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    MainNavigationBar(
                        selectedDestination = selectedDestination,
                        onSelect = navController::navigateToTopLevel,
                    )
                },
            ) { paddingValues ->
                HomeNavHost(
                    navController = navController,
                    onNavigateToBreedsDetails = onNavigateToBreedsDetails,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun HomeNavHost(
    navController: NavHostController,
    onNavigateToBreedsDetails: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navController,
        startDestination = BreedsDestination,
        enterTransition = { fadeIn(tween(TOP_LEVEL_TRANSITION_MILLIS)) },
        exitTransition = { fadeOut(tween(TOP_LEVEL_TRANSITION_MILLIS)) },
        popEnterTransition = { fadeIn(tween(TOP_LEVEL_TRANSITION_MILLIS)) },
        popExitTransition = { fadeOut(tween(TOP_LEVEL_TRANSITION_MILLIS)) },
    ) {
        breedsScreen(onNavigateToBreedsDetails)
        settingsScreen()
    }
}

@Composable
private fun MainNavigationBar(
    selectedDestination: MainDestination,
    onSelect: (MainDestination) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_breeds)) },
            onClick = { onSelect(MainDestination.BREEDS) },
            selected = selectedDestination == MainDestination.BREEDS,
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
            onClick = { onSelect(MainDestination.SETTINGS) },
            selected = selectedDestination == MainDestination.SETTINGS,
        )
    }
}

@Composable
private fun MainNavigationRail(
    selectedDestination: MainDestination,
    onSelect: (MainDestination) -> Unit,
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        NavigationRailItem(
            selected = selectedDestination == MainDestination.BREEDS,
            onClick = { onSelect(MainDestination.BREEDS) },
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_breeds)) },
        )
        NavigationRailItem(
            selected = selectedDestination == MainDestination.SETTINGS,
            onClick = { onSelect(MainDestination.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

private inline fun <reified T : Any> Sequence<NavDestination>?.hasRoute(): Boolean {
    return this?.any { it.hasRoute(T::class) } == true
}

private fun NavHostController.navigateToTopLevel(destination: MainDestination) {
    val route = when (destination) {
        MainDestination.BREEDS -> BreedsDestination
        MainDestination.SETTINGS -> SettingsDestination
    }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private enum class MainDestination {
    BREEDS,
    SETTINGS,
}

private val NAVIGATION_RAIL_BREAKPOINT = 600.dp
private const val TOP_LEVEL_TRANSITION_MILLIS = 180
