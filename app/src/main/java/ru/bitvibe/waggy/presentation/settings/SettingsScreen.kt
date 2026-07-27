package ru.bitvibe.waggy.presentation.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
    val context = LocalContext.current

    fun launchInstaller(apkUri: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri.toUri(), APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { context.startActivity(intent) }
            .onSuccess { viewModel.onEvent(SettingsEvent.OnInstallerLaunched) }
            .onFailure { viewModel.onEvent(SettingsEvent.OnInstallLaunchFailed) }
    }

    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        val apkUri = when (val updateState = state.appUpdateState) {
            is AppUpdateUiState.ReadyToInstall -> updateState.apkUri
            is AppUpdateUiState.Downloaded -> updateState.apkUri
            else -> null
        }
        if (context.canInstallUnknownApps() && apkUri != null) {
            launchInstaller(apkUri)
        } else {
            viewModel.onEvent(SettingsEvent.OnInstallPermissionDenied)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingsEvent.OnRefresh)
        viewModel.onEvent(SettingsEvent.OnCheckForUpdate)
    }

    LaunchedEffect(state.appUpdateState) {
        val readyUpdate = state.appUpdateState as? AppUpdateUiState.ReadyToInstall
            ?: return@LaunchedEffect
        if (context.canInstallUnknownApps()) {
            launchInstaller(readyUpdate.apkUri)
        } else {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                "package:${context.packageName}".toUri(),
            )
            runCatching { installPermissionLauncher.launch(permissionIntent) }
                .onFailure { viewModel.onEvent(SettingsEvent.OnInstallLaunchFailed) }
        }
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
                AboutSettingsCard(
                    appUpdateState = state.appUpdateState,
                    onCheckForUpdate = {
                        viewModel.onEvent(SettingsEvent.OnCheckForUpdate)
                    },
                    onDownloadUpdate = {
                        viewModel.onEvent(SettingsEvent.OnDownloadUpdate)
                    },
                    onInstallUpdate = { apkUri ->
                        if (context.canInstallUnknownApps()) {
                            launchInstaller(apkUri)
                        } else {
                            val permissionIntent = Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                "package:${context.packageName}".toUri(),
                            )
                            runCatching {
                                installPermissionLauncher.launch(permissionIntent)
                            }.onFailure {
                                viewModel.onEvent(SettingsEvent.OnInstallLaunchFailed)
                            }
                        }
                    },
                )
            }
        }
    }
}

private fun Context.canInstallUnknownApps(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
        packageManager.canRequestPackageInstalls()
}

private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
