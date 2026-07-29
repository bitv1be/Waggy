package ru.bitvibe.waggy.presentation.common

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.get
import coil3.Image
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

@Stable
data class PhotoPalette(
    val container: Color,
    val content: Color,
    val accent: Color,
    val onAccent: Color,
)

internal data class PhotoTones(
    val containerArgb: Int,
    val contentArgb: Int,
    val accentArgb: Int,
    val onAccentArgb: Int,
)

@Stable
class DominantPhotoColor internal constructor(
    val argb: Int?,
    val onImageLoaded: (Image) -> Unit,
)

@Composable
fun rememberDominantPhotoColor(imageKey: Any?): DominantPhotoColor {
    var dominantArgb by remember(imageKey) { mutableStateOf<Int?>(null) }
    var isExtracting by remember(imageKey) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    return DominantPhotoColor(
        argb = dominantArgb,
        onImageLoaded = { image ->
            if (dominantArgb == null && !isExtracting) {
                isExtracting = true
                coroutineScope.launch {
                    dominantArgb = withContext(Dispatchers.Default) {
                        extractDominantPhotoColor(image.toBitmap())
                    }
                    isExtracting = false
                }
            }
        },
    )
}

@Composable
fun photoPalette(dominantArgb: Int?): PhotoPalette {
    val colorScheme = MaterialTheme.colorScheme
    if (dominantArgb == null) {
        return PhotoPalette(
            container = colorScheme.surfaceContainerLow,
            content = colorScheme.onSurface,
            accent = colorScheme.primaryContainer,
            onAccent = colorScheme.onPrimaryContainer,
        )
    }

    val tones = createPhotoTones(
        sourceArgb = dominantArgb,
        darkTheme = colorScheme.background.luminance() < 0.5f,
    )
    return PhotoPalette(
        container = Color(tones.containerArgb),
        content = Color(tones.contentArgb),
        accent = Color(tones.accentArgb),
        onAccent = Color(tones.onAccentArgb),
    )
}

internal fun extractDominantPhotoColor(bitmap: Bitmap): Int? {
    if (bitmap.width <= 0 || bitmap.height <= 0) return null

    val buckets = mutableMapOf<Int, ColorBucket>()
    val stepX = max(1, bitmap.width / SAMPLE_GRID_SIZE)
    val stepY = max(1, bitmap.height / SAMPLE_GRID_SIZE)

    for (y in stepY / 2 until bitmap.height step stepY) {
        for (x in stepX / 2 until bitmap.width step stepX) {
            val color = bitmap[x, y]
            val alpha = color ushr 24 and 0xFF
            if (alpha < MIN_ALPHA) continue

            val red = color ushr 16 and 0xFF
            val green = color ushr 8 and 0xFF
            val blue = color and 0xFF
            val hsl = rgbToHsl(red, green, blue)
            if (hsl.lightness !in MIN_LIGHTNESS..MAX_LIGHTNESS) continue

            val key = (red / QUANTIZATION_STEP shl 8) or
                (green / QUANTIZATION_STEP shl 4) or
                (blue / QUANTIZATION_STEP)
            val bucket = buckets.getOrPut(key) { ColorBucket() }
            bucket.add(red, green, blue, 1.0 + hsl.saturation * SATURATION_WEIGHT)
        }
    }

    val dominant = buckets.values.maxByOrNull { it.score } ?: return null
    return argb(
        red = dominant.redTotal / dominant.sampleCount,
        green = dominant.greenTotal / dominant.sampleCount,
        blue = dominant.blueTotal / dominant.sampleCount,
    )
}

internal fun createPhotoTones(sourceArgb: Int, darkTheme: Boolean): PhotoTones {
    val source = rgbToHsl(
        red = sourceArgb ushr 16 and 0xFF,
        green = sourceArgb ushr 8 and 0xFF,
        blue = sourceArgb and 0xFF,
    )
    val hue = if (source.saturation < 0.05) DEFAULT_HUE else source.hue
    val container = hslToArgb(
        hue = hue,
        saturation = source.saturation.coerceIn(0.12, 0.28),
        lightness = if (darkTheme) 0.18 else 0.93,
    )
    val accent = hslToArgb(
        hue = hue,
        saturation = source.saturation.coerceIn(0.36, 0.64),
        lightness = if (darkTheme) 0.72 else 0.38,
    )

    return PhotoTones(
        containerArgb = container,
        contentArgb = bestContrastingForeground(container),
        accentArgb = accent,
        onAccentArgb = bestContrastingForeground(accent),
    )
}

internal fun contrastRatio(firstArgb: Int, secondArgb: Int): Double {
    val first = relativeLuminance(firstArgb)
    val second = relativeLuminance(secondArgb)
    return (max(first, second) + 0.05) / (min(first, second) + 0.05)
}

private fun bestContrastingForeground(backgroundArgb: Int): Int {
    val lightContrast = contrastRatio(backgroundArgb, LIGHT_FOREGROUND)
    val darkContrast = contrastRatio(backgroundArgb, DARK_FOREGROUND)
    return if (lightContrast >= darkContrast) LIGHT_FOREGROUND else DARK_FOREGROUND
}

private fun relativeLuminance(argb: Int): Double {
    fun channel(value: Int): Double {
        val normalized = value / 255.0
        return if (normalized <= 0.04045) {
            normalized / 12.92
        } else {
            ((normalized + 0.055) / 1.055).pow(2.4)
        }
    }

    val red = channel(argb ushr 16 and 0xFF)
    val green = channel(argb ushr 8 and 0xFF)
    val blue = channel(argb and 0xFF)
    return red * 0.2126 + green * 0.7152 + blue * 0.0722
}

private fun rgbToHsl(red: Int, green: Int, blue: Int): HslColor {
    val redValue = red / 255.0
    val greenValue = green / 255.0
    val blueValue = blue / 255.0
    val maximum = max(redValue, max(greenValue, blueValue))
    val minimum = min(redValue, min(greenValue, blueValue))
    val delta = maximum - minimum
    val lightness = (maximum + minimum) / 2.0

    if (abs(delta) < 0.00001) {
        return HslColor(hue = 0.0, saturation = 0.0, lightness = lightness)
    }

    val saturation = delta / (1.0 - abs(2.0 * lightness - 1.0))
    val hue = when (maximum) {
        redValue -> 60.0 * (((greenValue - blueValue) / delta) % 6.0)
        greenValue -> 60.0 * (((blueValue - redValue) / delta) + 2.0)
        else -> 60.0 * (((redValue - greenValue) / delta) + 4.0)
    }.let { if (it < 0.0) it + 360.0 else it }

    return HslColor(hue = hue, saturation = saturation, lightness = lightness)
}

private fun hslToArgb(hue: Double, saturation: Double, lightness: Double): Int {
    val chroma = (1.0 - abs(2.0 * lightness - 1.0)) * saturation
    val hueSegment = hue / 60.0
    val secondary = chroma * (1.0 - abs(hueSegment % 2.0 - 1.0))
    val (redPrime, greenPrime, bluePrime) = when (hueSegment) {
        in 0.0..<1.0 -> Triple(chroma, secondary, 0.0)
        in 1.0..<2.0 -> Triple(secondary, chroma, 0.0)
        in 2.0..<3.0 -> Triple(0.0, chroma, secondary)
        in 3.0..<4.0 -> Triple(0.0, secondary, chroma)
        in 4.0..<5.0 -> Triple(secondary, 0.0, chroma)
        else -> Triple(chroma, 0.0, secondary)
    }
    val match = lightness - chroma / 2.0
    return argb(
        red = ((redPrime + match) * 255.0).roundToInt(),
        green = ((greenPrime + match) * 255.0).roundToInt(),
        blue = ((bluePrime + match) * 255.0).roundToInt(),
    )
}

private fun argb(red: Int, green: Int, blue: Int): Int {
    return (0xFF shl 24) or
        (red.coerceIn(0, 255) shl 16) or
        (green.coerceIn(0, 255) shl 8) or
        blue.coerceIn(0, 255)
}

private data class HslColor(
    val hue: Double,
    val saturation: Double,
    val lightness: Double,
)

private class ColorBucket {
    var redTotal: Int = 0
    var greenTotal: Int = 0
    var blueTotal: Int = 0
    var sampleCount: Int = 0
    var score: Double = 0.0

    fun add(red: Int, green: Int, blue: Int, weight: Double) {
        redTotal += red
        greenTotal += green
        blueTotal += blue
        sampleCount += 1
        score += weight
    }
}

private const val SAMPLE_GRID_SIZE = 32
private const val MIN_ALPHA = 192
private const val QUANTIZATION_STEP = 24
private const val MIN_LIGHTNESS = 0.06
private const val MAX_LIGHTNESS = 0.96
private const val SATURATION_WEIGHT = 1.4
private const val DEFAULT_HUE = 145.0
private const val LIGHT_FOREGROUND = -0x1
private const val DARK_FOREGROUND = -0xe9e7e7
