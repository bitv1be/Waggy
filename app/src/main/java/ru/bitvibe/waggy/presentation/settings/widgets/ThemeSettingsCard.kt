package ru.bitvibe.waggy.presentation.settings.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSettingsCard(
    isDarkTheme: Boolean?,
    onSetTheme: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isDarkTheme == null,
                    onClick = { onSetTheme(null) })
                Text("System Default")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isDarkTheme == false,
                    onClick = { onSetTheme(false) })
                Text("Light")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isDarkTheme == true,
                    onClick = { onSetTheme(true) })
                Text("Dark")
            }
        }
    }
}
