package ru.bitvibe.waggy.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.bitvibe.waggy.domain.preferences.WidgetPreferences

class WidgetPreferencesImpl(
    context: Context
) : WidgetPreferences {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

    private val _updatePeriodMinutes = MutableStateFlow<Long>(
        prefs.getLong("update_period_minutes", 15L)
    )
    override val updatePeriodMinutes: StateFlow<Long> = _updatePeriodMinutes.asStateFlow()

    override fun setUpdatePeriodMinutes(minutes: Long) {
        prefs.edit { putLong("update_period_minutes", minutes) }
        _updatePeriodMinutes.value = minutes
    }
}
