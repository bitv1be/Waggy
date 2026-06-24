package ru.bitvibe.waggy.presentation.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.bitvibe.waggy.BuildConfig
import ru.bitvibe.waggy.domain.usecase.GetRandomFavoriteBreedUseCase
import ru.bitvibe.waggy.domain.usecase.UseCase
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

@HiltWorker
class BreedWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val getRandomFavoriteBreedUseCase: GetRandomFavoriteBreedUseCase
) : CoroutineWorker(context, params) {
    companion object {
        const val APP_WIDGET_ID_EXTRA = "app_widget_id_extra"
        const val TAG = "BreedWidgetWorker"

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

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    private suspend fun updateWidget(glanceId: GlanceId) = withContext(Dispatchers.IO) {
        updateAppWidgetState(
            context = context,
            definition = BreedWidgetStateDefinition,
            glanceId = glanceId,
            updateState = { BreedWidgetState.Loading }
        )
        BreedAppWidget.update(context, glanceId)

        val newBreed = getRandomFavoriteBreedUseCase(UseCase.None) ?: run {
            setErrorWidget(glanceId)
            return@withContext
        }

        val fullImageUrl =
            "${BuildConfig.BASE_URL.trimEnd('/')}/${newBreed.imageUrl.trimStart('/')}"

        val bitmap = try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(fullImageUrl)
                .allowHardware(false)
                .build()

            when (val result = loader.execute(request)) {
                is SuccessResult -> {
                    result.image.toBitmap()
                }

                is ErrorResult -> {
                    setErrorWidget(glanceId, "Failed to get image")
                    return@withContext
                }
            }
        } catch (e: Exception) {
            val message = e.message ?: "Unknown error"
            Log.e(TAG, message)
            setErrorWidget(glanceId, "Failed to get image")
            return@withContext
        }

        val foreground = withContext(Dispatchers.Default) {
            segmentDog(bitmap)?.let { image ->
                val stream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.WEBP, 100, stream)
                stream.toByteArray()
            }
        }

        val background = createClippedWidgetBitmap(
            bitmap, bitmap.width, bitmap.height,
            MaterialShapes.SemiCircle
        ).let { image ->
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.WEBP, 100, stream)
            stream.toByteArray()
        }

        updateAppWidgetState(
            context = context,
            definition = BreedWidgetStateDefinition,
            glanceId = glanceId,
            updateState = {
                BreedWidgetState.Loaded(
                    breedName = newBreed.breedName,
                    subBreedName = newBreed.subBreedName,
                    backgroundImage = background,
                    foregroundImage = foreground
                )
            }
        )
        BreedAppWidget.update(context, glanceId)
    }

    private suspend fun segmentDog(bitmap: Bitmap): Bitmap? = suspendCancellableCoroutine { cont ->
        val subjectResultOptions = SubjectSegmenterOptions.SubjectResultOptions.Builder()
            .enableSubjectBitmap()
            .build()

        val options = SubjectSegmenterOptions.Builder()
            .enableMultipleSubjects(subjectResultOptions)
            .build()

        val segmenter = SubjectSegmentation.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)

        segmenter.process(image)
            .addOnSuccessListener { result ->
                cont.resume(result.subjects.firstOrNull()?.bitmap)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Segmentation failed", error)
                cont.resume(null)
            }
    }

    private suspend fun setErrorWidget(
        glanceId: GlanceId,
        message: String = "Failed to get new breed"
    ) {
        updateAppWidgetState(
            context = context,
            definition = BreedWidgetStateDefinition,
            glanceId = glanceId,
            updateState = {
                BreedWidgetState.Error(message)
            }
        )
        BreedAppWidget.update(context, glanceId)
    }

    private suspend fun createClippedWidgetBitmap(
        inputBitmap: Bitmap,
        widthPx: Int,
        heightPx: Int,
        polygon: RoundedPolygon
    ): Bitmap = withContext(Dispatchers.Default) {
        val basePath = polygon.toPath()

        val pathScaleMatrix = Matrix().apply {
            setScale(widthPx.toFloat(), heightPx.toFloat())
        }
        val scaledPath = Path()
        basePath.transform(pathScaleMatrix, scaledPath)

        val output = createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawPath(scaledPath, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val scale: Float
        val dx: Float
        val dy: Float

        if (inputBitmap.width * heightPx > widthPx * inputBitmap.height) {
            scale = heightPx.toFloat() / inputBitmap.height.toFloat()
            dx = (widthPx - inputBitmap.width * scale) * 0.2f
            dy = 0f
        } else {
            scale = widthPx.toFloat() / inputBitmap.width.toFloat()
            dx = 0f
            dy = (heightPx - inputBitmap.height * scale) * 0.2f
        }

        val bitmapMatrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }

        canvas.drawBitmap(inputBitmap, bitmapMatrix, paint)

        return@withContext output
    }
}