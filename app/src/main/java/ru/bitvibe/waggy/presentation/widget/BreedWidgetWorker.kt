package ru.bitvibe.waggy.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil3.toBitmap
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ru.bitvibe.waggy.domain.usecase.GetRandomFavoriteBreedUseCase
import ru.bitvibe.waggy.domain.usecase.UseCase
import java.util.concurrent.TimeUnit

@HiltWorker
class BreedWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val getRandomFavoriteBreedUseCase: GetRandomFavoriteBreedUseCase
) : CoroutineWorker(context, params) {
    companion object {
        const val APP_WIDGET_ID_EXTRA = "app_widget_id_extra"

        fun enqueuePeriodicWork(
            context: Context,
            appWidgetId: Int,
            force: Boolean = false
        ) {
            val workManager = WorkManager.getInstance(context)

            val inputData = Data.Builder()
                .putInt(APP_WIDGET_ID_EXTRA, appWidgetId)
                .build()

            val uniqueWorkName = "${BreedWidgetWorker::class.java.simpleName}-$appWidgetId"

            val prefs = ru.bitvibe.waggy.data.preferences.WidgetPreferencesImpl(context)
            val intervalMinutes = prefs.updatePeriodMinutes.value

            if (intervalMinutes == -1L) {
                // Cancel periodic work if any
                workManager.cancelUniqueWork(uniqueWorkName)

                // Enqueue 30s one-time request
                val oneTimeRequest = OneTimeWorkRequestBuilder<BreedWidgetWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS)
                    .setInputData(inputData)
                    .build()
                workManager.enqueueUniqueWork(
                    uniqueWorkName + "_onetime",
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    oneTimeRequest
                )
            } else {
                // Cancel one-time work if any
                workManager.cancelUniqueWork(uniqueWorkName + "_onetime")

                val request =
                    PeriodicWorkRequestBuilder<BreedWidgetWorker>(intervalMinutes, TimeUnit.MINUTES)
                        .setInputData(inputData)
                        .build()

                workManager.enqueueUniquePeriodicWork(
                    uniqueWorkName = uniqueWorkName,
                    existingPeriodicWorkPolicy = if (force) {
                        ExistingPeriodicWorkPolicy.UPDATE
                    } else {
                        ExistingPeriodicWorkPolicy.KEEP
                    },
                    request = request
                )
            }
        }

        fun cancel(context: Context, appWidgetId: Int) {
            val uniqueWorkName = "${BreedWidgetWorker::class.java.simpleName}-$appWidgetId"
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName + "_onetime")
        }
    }

    override suspend fun doWork(): Result {
        val appWidgetManager = GlanceAppWidgetManager(context)

        val targetId = inputData.getInt(APP_WIDGET_ID_EXTRA, -1)

        if (targetId != -1) {
            val glanceId = appWidgetManager.getGlanceIdBy(targetId)
            updateWidget(glanceId)
        } else {
            appWidgetManager.getGlanceIds(BreedAppWidget::class.java).forEach { glanceId ->
                updateWidget(glanceId)
            }
        }

        val prefs = ru.bitvibe.waggy.data.preferences.WidgetPreferencesImpl(context)
        if (prefs.updatePeriodMinutes.value == -1L && targetId != -1) {
            enqueuePeriodicWork(context, targetId, true)
        }

        return Result.success()
    }

    private suspend fun updateWidget(glanceId: GlanceId) {
        updateAppWidgetState(
            context = context,
            definition = BreedWidgetStateDefinition,
            glanceId = glanceId,
            updateState = { BreedWidgetState.Loading }
        )
        BreedAppWidget.update(context, glanceId)

        val newBreed = getRandomFavoriteBreedUseCase(UseCase.None)

        var localImagePath: String? = null
        if (newBreed != null && newBreed.imageUrl.isNotEmpty()) {
            try {
                val loader = coil3.ImageLoader(context)
                val request = coil3.request.ImageRequest.Builder(context)
                    .data(newBreed.imageUrl)
                    .size(500)
                    .build()
                val result = loader.execute(request)
                if (result is coil3.request.SuccessResult) {
                    val bitmap = result.image.toBitmap()
                    val file = java.io.File(context.cacheDir, "widget_${glanceId}_image.jpeg")
                    file.outputStream().use {
                        bitmap.compress(
                            android.graphics.Bitmap.CompressFormat.JPEG,
                            90,
                            it
                        )
                    }
                    localImagePath = file.absolutePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        updateAppWidgetState(
            context = context,
            definition = BreedWidgetStateDefinition,
            glanceId = glanceId,
            updateState = {
                if (newBreed != null) {
                    BreedWidgetState.Loaded(
                        newBreed.breedName,
                        localImagePath ?: newBreed.imageUrl,
                        newBreed.subBreedName
                    )
                } else {
                    BreedWidgetState.Error("Failed to get new breed")
                }
            }
        )
        BreedAppWidget.update(context, glanceId)
    }
}