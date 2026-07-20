package ru.bitvibe.waggy.presentation.widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import ru.bitvibe.waggy.R

object BreedAppWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = BreedWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                when (val widgetState = currentState<BreedWidgetState>()) {
                    is BreedWidgetState.Loaded -> BreedWidgetContent(
                        context,
                        widgetState
                    )

                    is BreedWidgetState.Loading -> {
                        Box(
                            modifier = GlanceModifier.fillMaxSize()
                                .background(GlanceTheme.colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GlanceTheme.colors.onPrimaryContainer)
                        }
                    }

                    is BreedWidgetState.Error -> {
                        Column(
                            modifier = GlanceModifier.fillMaxSize()
                                .background(GlanceTheme.colors.primaryContainer),
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                        ) {
                            Text(
                                widgetState.message, style = TextStyle(
                                    color = GlanceTheme.colors.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(12.dp))
                            Button(
                                text =
                                    context.getString(R.string.try_again),
                                onClick = actionRunCallback<RefreshAction>(),
                                modifier = GlanceModifier.padding(16.dp, 8.dp)
                                    .background(GlanceTheme.colors.primary),
                                style = TextStyle(
                                    color = GlanceTheme.colors.onPrimary,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @GlanceComposable
    @Composable
    fun BreedWidgetContent(
        context: Context,
        state: BreedWidgetState.Loaded
    ) {
        val backgroundColor = GlanceTheme.colors.primaryContainer.getColor(context)
            .copy(alpha = 0.75f)

        Box(
            modifier = GlanceModifier.fillMaxSize()
                .background(
                    ImageProvider(
                        BitmapFactory.decodeByteArray(
                            state.backgroundImage,
                            0,
                            state.backgroundImage.size
                        )
                    )
                )
                .clickable(onClick = actionRunCallback<RefreshAction>()),
        ) {
            if (state.foregroundImage != null) {
                Image(
                    provider = ImageProvider(
                        BitmapFactory.decodeByteArray(
                            state.foregroundImage,
                            0,
                            state.foregroundImage.size
                        )
                    ),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
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
                        state.subBreedName?.let { "$it ${state.breedName}" }
                            ?: state.breedName

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
    }
}