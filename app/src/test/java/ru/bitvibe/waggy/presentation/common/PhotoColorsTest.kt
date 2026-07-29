package ru.bitvibe.waggy.presentation.common

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoColorsTest {

    @Test
    fun createPhotoTones_providesAccessibleContrastInLightAndDarkThemes() {
        val sourceColors = listOf(
            0xFF8A5A3C.toInt(),
            0xFFDFC8A7.toInt(),
            0xFF243A28.toInt(),
            0xFF567D9A.toInt(),
            0xFF8C8C8C.toInt(),
        )

        sourceColors.forEach { sourceColor ->
            listOf(false, true).forEach { darkTheme ->
                val tones = createPhotoTones(sourceColor, darkTheme)

                assertTrue(
                    "Container text contrast was below 4.5 for ${sourceColor.toUInt().toString(16)}",
                    contrastRatio(tones.containerArgb, tones.contentArgb) >= MIN_TEXT_CONTRAST,
                )
                assertTrue(
                    "Accent text contrast was below 4.5 for ${sourceColor.toUInt().toString(16)}",
                    contrastRatio(tones.accentArgb, tones.onAccentArgb) >= MIN_TEXT_CONTRAST,
                )
            }
        }
    }

    @Test
    fun createPhotoTones_preservesPhotoIdentityWithoutReusingRawColor() {
        val warmSource = 0xFF9A5A38.toInt()
        val coolSource = 0xFF397A91.toInt()

        val warmTones = createPhotoTones(warmSource, darkTheme = false)
        val coolTones = createPhotoTones(coolSource, darkTheme = false)

        assertNotEquals(warmSource, warmTones.containerArgb)
        assertNotEquals(coolSource, coolTones.containerArgb)
        assertNotEquals(warmTones.containerArgb, coolTones.containerArgb)
        assertNotEquals(warmTones.accentArgb, coolTones.accentArgb)
    }

    @Test
    fun createPhotoTones_adaptsContainerToneToTheme() {
        val source = 0xFF7A6645.toInt()

        val lightTones = createPhotoTones(source, darkTheme = false)
        val darkTones = createPhotoTones(source, darkTheme = true)

        assertNotEquals(lightTones.containerArgb, darkTones.containerArgb)
        assertTrue(
            contrastRatio(lightTones.containerArgb, darkTones.containerArgb) >
                MIN_THEME_CONTAINER_CONTRAST,
        )
    }

    private companion object {
        const val MIN_TEXT_CONTRAST = 4.5
        const val MIN_THEME_CONTAINER_CONTRAST = 6.0
    }
}
