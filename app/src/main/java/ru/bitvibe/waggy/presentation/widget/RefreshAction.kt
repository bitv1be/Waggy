package ru.bitvibe.waggy.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(
            context = context,
            definition = BreedWidgetStateDefinition,
            glanceId = glanceId
        ) {
            BreedWidgetState.Loading
        }
        BreedAppWidget.update(context, glanceId)

        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        val inputData = Data.Builder()
            .putInt(BreedWidgetWorker.APP_WIDGET_ID_EXTRA, appWidgetId)
            .build()

        val refreshRequest = OneTimeWorkRequestBuilder<BreedWidgetWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(refreshRequest)
    }
}