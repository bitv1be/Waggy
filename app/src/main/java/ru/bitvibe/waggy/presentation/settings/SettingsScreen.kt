package ru.bitvibe.waggy.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.bitvibe.waggy.presentation.settings.widgets.AboutSettingsCard
import ru.bitvibe.waggy.presentation.settings.widgets.FavoriteItemCard
import ru.bitvibe.waggy.presentation.settings.widgets.ThemeSettingsCard
import ru.bitvibe.waggy.presentation.settings.widgets.WidgetSettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val widgetPeriodMinutes by viewModel.widgetPeriodMinutes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingsEvent.OnRefresh)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Appearance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ThemeSettingsCard(
                    isDarkTheme = isDarkTheme,
                    onSetTheme = { viewModel.onEvent(SettingsEvent.OnSetTheme(it)) }
                )
            }

            item {
                Text("Widget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                WidgetSettingsCard(
                    widgetPeriodMinutes = widgetPeriodMinutes,
                    onSetWidgetPeriod = { viewModel.onEvent(SettingsEvent.OnSetWidgetPeriod(it)) }
                )
            }

            item {
                Text("Favorites", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (state.favorites.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.onEvent(SettingsEvent.OnClearAll) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All Favorites")
                    }
                }
            }

            if (state.isLoading && state.favorites.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.favorites.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No favorites yet!")
                    }
                }
            } else {
                items(state.favorites) { favorite ->
                    FavoriteItemCard(
                        favorite = favorite,
                        onRemove = { viewModel.onEvent(SettingsEvent.OnRemove(favorite)) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                AboutSettingsCard()
            }
        }
    }
}