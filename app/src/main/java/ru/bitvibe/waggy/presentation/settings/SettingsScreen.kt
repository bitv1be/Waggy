package ru.bitvibe.waggy.presentation.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.models.Favorite
import ru.bitvibe.waggy.domain.preferences.ThemeMode
import ru.bitvibe.waggy.presentation.settings.widgets.AboutSettingsCard
import ru.bitvibe.waggy.presentation.settings.widgets.FavoriteItemCard
import ru.bitvibe.waggy.presentation.settings.widgets.ThemeSettingsCard
import ru.bitvibe.waggy.presentation.settings.widgets.WidgetSettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val widgetPeriodMinutes by viewModel.widgetPeriodMinutes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
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
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = SETTINGS_COLUMN_MIN_WIDTH),
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues),
            contentPadding = PaddingValues(
                start = paddingValues.calculateLeftPadding(layoutDirection) + 16.dp,
                top = paddingValues.calculateTopPadding() + 12.dp,
                end = paddingValues.calculateRightPadding(layoutDirection) + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "appearance") {
                SettingsSection(title = stringResource(R.string.appearance_section)) {
                    ThemeSettingsCard(
                        isDarkTheme = isDarkTheme,
                        onSetTheme = { viewModel.onEvent(SettingsEvent.OnSetTheme(it)) },
                    )
                }
            }

            item(key = "widget") {
                SettingsSection(title = stringResource(R.string.widget_section)) {
                    WidgetSettingsCard(
                        widgetPeriodMinutes = widgetPeriodMinutes,
                        onSetWidgetPeriod = {
                            viewModel.onEvent(SettingsEvent.OnSetWidgetPeriod(it))
                        },
                    )
                }
            }

            item(key = "favorites") {
                SettingsSection(
                    title = stringResource(R.string.favorites_section),
                    action = if (state.favorites.isNotEmpty()) {
                        {
                            TextButton(
                                onClick = { viewModel.onEvent(SettingsEvent.OnClearAll) },
                            ) {
                                Text(
                                    text = stringResource(R.string.clear_all_favorites),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    } else {
                        null
                    },
                ) {
                    FavoritesSettingsCard(
                        favorites = state.favorites,
                        isLoading = state.isLoading,
                        onRemove = { favorite ->
                            viewModel.onEvent(SettingsEvent.OnRemove(favorite))
                        },
                    )
                }
            }

            item(key = "about") {
                SettingsSection(title = stringResource(R.string.about_section)) {
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
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            action?.invoke()
        }
        content()
    }
}

@Composable
private fun FavoritesSettingsCard(
    favorites: List<Favorite>,
    isLoading: Boolean,
    onRemove: (Favorite) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        when {
            isLoading && favorites.isEmpty() -> {
                val loadingDescription = stringResource(R.string.loading_favorites)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = loadingDescription
                        },
                    )
                }
            }

            favorites.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.no_favorites_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.no_favorites_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                Column {
                    favorites.forEachIndexed { index, favorite ->
                        FavoriteItemCard(
                            favorite = favorite,
                            onRemove = { onRemove(favorite) },
                        )
                        if (index != favorites.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Context.canInstallUnknownApps(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            packageManager.canRequestPackageInstalls()
}

private val SETTINGS_COLUMN_MIN_WIDTH = 340.dp
private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
