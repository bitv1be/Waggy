package ru.bitvibe.waggy.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil3.toBitmap
import kotlinx.coroutines.runBlocking

object BreedAppWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = BreedWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier.fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .clickable(onClick = actionRunCallback<RefreshAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    when (val widgetState = currentState<BreedWidgetState>()) {
                        is BreedWidgetState.Loaded -> BreedWidgetContent(
                            context,
                            widgetState.breedName,
                            widgetState.imageUrl,
                            widgetState.subBreedName
                        )

                        is BreedWidgetState.Loading -> {
                            CircularProgressIndicator()
                        }

                        is BreedWidgetState.Error -> {
                            Text(widgetState.message)
                        }

                    }
                }
            }

        }
    }
}

@GlanceComposable
@Composable
fun BreedWidgetContent(
    context: Context,
    breedName: String,
    imageUrl: String,
    subBreedName: String?,
) {
    val bitmap = if (imageUrl.isNotEmpty()) {
        try {
            android.graphics.BitmapFactory.decodeFile(imageUrl)
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }

    val backgroundColor = GlanceTheme.colors.background.getColor(context)
        .copy(alpha = 0.75f)

    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }


    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = GlanceModifier
                .padding(8.dp)
                .cornerRadius(8.dp)
                .background(
                    backgroundColor
                )
        ) {
            val breedText =
                subBreedName?.let { "$it $breedName" }
                    ?: breedName

            Text(
                text = breedText.replaceFirstChar { it.uppercase() },
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GlanceTheme.colors.onBackground
                )
            )
        }
    }
}

//private fun update(
//    context: Context
//) = {
//    GlanceAppWidgetManager(context = context).getGlanceIds(BreedAppWidget::class.java)
//        .forEach { glanceId ->
//            updateAppWidgetState(context, glanceId) {}
//            BreedAppWidget().update(context, glanceId)
//        }
//}