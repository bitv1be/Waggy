package ru.bitvibe.waggy.presentation.settings.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.domain.preferences.ThemeMode

@Composable
fun ThemeSettingsCard(
    isDarkTheme: ThemeMode,
    onSetTheme: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .selectableGroup(),
        ) {
            Text(
                text = stringResource(R.string.theme_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.theme_description),
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingsRadioOption(
                text = stringResource(R.string.theme_system),
                selected = isDarkTheme == ThemeMode.SYSTEM,
                onClick = { onSetTheme(ThemeMode.SYSTEM) },
            )
            SettingsRadioOption(
                text = stringResource(R.string.theme_light),
                selected = isDarkTheme == ThemeMode.LIGHT,
                onClick = { onSetTheme(ThemeMode.LIGHT) },
            )
            SettingsRadioOption(
                text = stringResource(R.string.theme_dark),
                selected = isDarkTheme == ThemeMode.DARK,
                onClick = { onSetTheme(ThemeMode.DARK) },
            )
        }
    }
}

@Composable
internal fun SettingsRadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
