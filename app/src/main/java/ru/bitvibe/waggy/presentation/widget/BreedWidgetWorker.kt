package ru.bitvibe.waggy.presentation.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
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
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.bitvibe.waggy.BuildConfig
import ru.bitvibe.waggy.R
import ru.bitvibe.waggy.data.preferences.WidgetPreferencesImpl
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
        private const val ONE_TIME_POSTFIX = "_onetime"
        private const val TAG = "BreedWidgetWorker"

        private val SEGMENTER_OPTIONS = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()

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
            val prefs = WidgetPreferencesImpl(context)
            val intervalMinutes = prefs.updatePeriodMinutes.value

            if (intervalMinutes == -1L) {
                workManager.cancelUniqueWork(uniqueWorkName)

                val oneTimeRequest = OneTimeWorkRequestBuilder<BreedWidgetWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS)
                    .setInputData(inputData)
                    .build()
                workManager.enqueueUniqueWork(
                    uniqueWorkName + ONE_TIME_POSTFIX,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    oneTimeRequest
                )
            } else {
                workManager.cancelUniqueWork(uniqueWorkName + ONE_TIME_POSTFIX)

                val request =
                    PeriodicWorkRequestBuilder<BreedWidgetWorker>(intervalMinutes, TimeUnit.MINUTES)
                        .setInputData(inputData)
                        .build()

                workManager.enqueueUniquePeriodicWork(
                    uniqueWorkName = uniqueWorkName,
                    existingPeriodicWorkPolicy = if (force) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                    request = request
                )
            }
        }

        fun cancel(context: Context, appWidgetId: Int) {
            val uniqueWorkName = "${BreedWidgetWorker::class.java.simpleName}-$appWidgetId"
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(uniqueWorkName)
            workManager.cancelUniqueWork(uniqueWorkName + ONE_TIME_POSTFIX)
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
            setErrorWidget(glanceId, context.getString(R.string.failed_get_new_breed))
            return@withContext
        }

        val fullImageUrl =
            "${BuildConfig.BASE_URL.trimEnd('/')}/${newBreed.imageUrl.trimStart('/')}"

        val bitmap = try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(fullImageUrl)
                .allowHardware(false)
                .size(800)
                .build()

            when (val result = loader.execute(request)) {
                is SuccessResult -> result.image.toBitmap()
                is ErrorResult -> {
                    Firebase.crashlytics.recordException(result.throwable)
                    Log.e(TAG, "Downloading image failed", result.throwable)
                    setErrorWidget(glanceId, context.getString(R.string.failed_get_image))
                    return@withContext
                }
            }
        } catch (e: Exception) {
            val message = e.message ?: context.getString(R.string.unknown_error)
            Log.e(TAG, message, e)
            Firebase.crashlytics.log(message)
            Firebase.crashlytics.recordException(e)
            setErrorWidget(glanceId, context.getString(R.string.failed_get_image))
            return@withContext
        }

        try {
            coroutineScope {
                val foregroundDeferred = async(Dispatchers.Default) {
                    segmentDog(bitmap)?.let { image ->
                        try {
                            val size = maxOf(bitmap.width, bitmap.height)
                            val aligned = createAlignedWidgetBitmap(
                                inputBitmap = image,
                                widthPx = size,
                                heightPx = size,
                                polygon = MaterialShapes.SemiCircle,
                                clipToPolygon = false
                            )
                            try {
                                aligned.toCompressedByteArray(quality = 80)
                            } finally {
                                aligned.recycle()
                            }
                        } finally {
                            image.recycle()
                        }
                    }
                }

                val backgroundDeferred = async(Dispatchers.Default) {
                    val size = maxOf(bitmap.width, bitmap.height)
                    val clipped = createAlignedWidgetBitmap(
                        inputBitmap = bitmap,
                        widthPx = size,
                        heightPx = size,
                        polygon = MaterialShapes.SemiCircle,
                        clipToPolygon = true
                    )
                    try {
                        clipped.toCompressedByteArray(quality = 80)
                    } finally {
                        clipped.recycle()
                    }
                }

                val foreground = foregroundDeferred.await()
                val background = backgroundDeferred.await()

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
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun segmentDog(bitmap: Bitmap): Bitmap? = suspendCancellableCoroutine { cont ->
        val segmenter = SubjectSegmentation.getClient(SEGMENTER_OPTIONS)

        cont.invokeOnCancellation { segmenter.close() }

        segmenter.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                if (cont.isActive) cont.resume(result.foregroundBitmap)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Segmentation failed", error)
                Firebase.crashlytics.log("Widget segmentation failed")
                Firebase.crashlytics.recordException(error)
                if (cont.isActive) cont.resume(null)
            }
            .addOnCompleteListener {
                segmenter.close()
            }
    }

    private suspend fun setErrorWidget(glanceId: GlanceId, message: String) {
        updateAppWidgetState(
            context = context,
            definition = BreedWidgetStateDefinition,
            glanceId = glanceId,
            updateState = { BreedWidgetState.Error(message) }
        )
        BreedAppWidget.update(context, glanceId)
    }

    private suspend fun createAlignedWidgetBitmap(
        inputBitmap: Bitmap,
        widthPx: Int,
        heightPx: Int,
        polygon: RoundedPolygon,
        clipToPolygon: Boolean
    ): Bitmap = withContext(Dispatchers.Default) {
        val output = createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (clipToPolygon) {
            val basePath = polygon.toPath()
            val pathScaleMatrix = Matrix().apply {
                setScale(widthPx.toFloat(), heightPx.toFloat())
            }
            val scaledPath = Path()
            basePath.transform(pathScaleMatrix, scaledPath)
            canvas.drawPath(scaledPath, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }

        val scale: Float
        val dx: Float
        val dy: Float

        if (inputBitmap.width * heightPx > widthPx * inputBitmap.height) {
            scale = heightPx.toFloat() / inputBitmap.height.toFloat()
            dx = (widthPx - inputBitmap.width * scale) * 0.5f
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

    private fun Bitmap.toCompressedByteArray(quality: Int): ByteArray {
        return ByteArrayOutputStream(1024 * 64).use { stream ->
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.WEBP
            }
            compress(format, quality, stream)
            stream.toByteArray()
        }
    }
}
