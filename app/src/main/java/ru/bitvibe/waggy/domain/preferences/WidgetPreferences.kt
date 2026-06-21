package ru.bitvibe.waggy.domain.preferences

import kotlinx.coroutines.flow.StateFlow

interface WidgetPreferences {
    val updatePeriodMinutes: StateFlow<Long>
    fun setUpdatePeriodMinutes(minutes: Long)
}
