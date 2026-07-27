package ru.bitvibe.waggy.presentation.settings.widgets

import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.BuildConfig
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.presentation.settings.AppUpdateOperation
import ru.bitvibe.waggy.presentation.settings.AppUpdateUiState

@Composable
fun AboutSettingsCard(
    appUpdateState: AppUpdateUiState,
    onCheckForUpdate: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.version_format,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                stringResource(R.string.updates_title),
                style = MaterialTheme.typography.titleSmall,
            )
            AppUpdateContent(
                state = appUpdateState,
                onCheckForUpdate = onCheckForUpdate,
                onDownloadUpdate = onDownloadUpdate,
                onInstallUpdate = onInstallUpdate,
            )
        }
    }
}

@Composable
private fun AppUpdateContent(
    state: AppUpdateUiState,
    onCheckForUpdate: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: (String) -> Unit,
) {
    when (state) {
        AppUpdateUiState.Idle -> {
            OutlinedButton(onClick = onCheckForUpdate) {
                Text(stringResource(R.string.check_for_updates))
            }
        }

        AppUpdateUiState.Checking -> UpdateStatusWithSpinner(
            text = stringResource(R.string.checking_for_updates),
        )

        AppUpdateUiState.UpToDate -> {
            Text(stringResource(R.string.app_is_up_to_date))
            OutlinedButton(onClick = onCheckForUpdate) {
                Text(stringResource(R.string.check_again))
            }
        }

        is AppUpdateUiState.Available -> {
            val context = LocalContext.current
            Text(
                stringResource(
                    R.string.update_available,
                    state.update.versionName,
                    Formatter.formatShortFileSize(context, state.update.sizeBytes),
                ),
            )
            state.update.releaseNotes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Button(onClick = onDownloadUpdate) {
                Text(stringResource(R.string.download_and_install))
            }
        }

        is AppUpdateUiState.Downloading -> {
            val progress = state.progressPercent
            if (progress == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.downloading_update))
            } else {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.downloading_update_progress, progress))
            }
        }

        is AppUpdateUiState.ReadyToInstall -> UpdateStatusWithSpinner(
            text = stringResource(R.string.opening_installer),
        )

        is AppUpdateUiState.Downloaded -> {
            val statusText = when {
                state.permissionDenied -> R.string.install_permission_needed
                state.installLaunchFailed -> R.string.could_not_open_installer
                else -> R.string.update_ready_to_install
            }
            Text(stringResource(statusText))
            Button(onClick = { onInstallUpdate(state.apkUri) }) {
                Text(stringResource(R.string.install_update))
            }
        }

        is AppUpdateUiState.Error -> {
            val errorText = when (state.operation) {
                AppUpdateOperation.CHECK -> R.string.update_check_failed
                AppUpdateOperation.DOWNLOAD -> R.string.update_download_failed
            }
            Text(
                text = stringResource(errorText),
                color = MaterialTheme.colorScheme.error,
            )
            OutlinedButton(onClick = onCheckForUpdate) {
                Text(stringResource(R.string.try_again))
            }
        }
    }
}

@Composable
private fun UpdateStatusWithSpinner(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
        )
        Text(text)
    }
}
