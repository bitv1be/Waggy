package ru.bitvibe.waggy.presentation.settings.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.bitvibe.waggy.R

@Composable
fun WidgetSettingsCard(
    widgetPeriodMinutes: Long,
    onSetWidgetPeriod: (Long) -> Unit,
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
                text = stringResource(R.string.widget_update_period),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            widgetPeriodOptions().forEach { option ->
                SettingsRadioOption(
                    text = stringResource(option.labelResource),
                    selected = widgetPeriodMinutes == option.minutes,
                    onClick = { onSetWidgetPeriod(option.minutes) },
                )
            }
        }
    }
}

@Composable
private fun widgetPeriodOptions(): List<WidgetPeriodOption> {
    return listOf(
        WidgetPeriodOption(-1L, R.string.widget_period_test),
        WidgetPeriodOption(15L, R.string.widget_period_15_minutes),
        WidgetPeriodOption(30L, R.string.widget_period_30_minutes),
        WidgetPeriodOption(60L, R.string.widget_period_1_hour),
        WidgetPeriodOption(180L, R.string.widget_period_3_hours),
        WidgetPeriodOption(720L, R.string.widget_period_12_hours),
        WidgetPeriodOption(1440L, R.string.widget_period_1_day),
    )
}

private data class WidgetPeriodOption(
    val minutes: Long,
    val labelResource: Int,
)
