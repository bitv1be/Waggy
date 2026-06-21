package ru.bitvibe.waggy.presentation.settings.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WidgetSettingsCard(
    widgetPeriodMinutes: Long,
    onSetWidgetPeriod: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Widget Autoupdate Period", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = widgetPeriodMinutes == -1L,
                    onClick = { onSetWidgetPeriod(-1L) })
                Text("30 Seconds (Test)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = widgetPeriodMinutes == 15L,
                    onClick = { onSetWidgetPeriod(15L) })
                Text("15 Minutes")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = widgetPeriodMinutes == 30L,
                    onClick = { onSetWidgetPeriod(30L) })
                Text("30 Minutes")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = widgetPeriodMinutes == 60L,
                    onClick = { onSetWidgetPeriod(60L) })
                Text("1 Hour")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = widgetPeriodMinutes == 180L,
                    onClick = { onSetWidgetPeriod(180L) })
                Text("3 Hours")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = widgetPeriodMinutes == 720L,
                    onClick = { onSetWidgetPeriod(720L) })
                Text("12 Hours")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = widgetPeriodMinutes == 1440L,
                    onClick = { onSetWidgetPeriod(1440L) })
                Text("1 Day")
            }
        }
    }
}
