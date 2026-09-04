package ru.bitvibe.waggy

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.bitvibe.waggy.domain.preferences.ThemeMode
import ru.bitvibe.waggy.domain.preferences.ThemePreferences
import ru.bitvibe.waggy.presentation.common.AppRoot
import ru.bitvibe.waggy.presentation.common.WaggyTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            DisposableEffect(useDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { useDarkTheme },
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { useDarkTheme },
                    ),
                )
                onDispose {}
            }

            WaggyTheme(darkTheme = useDarkTheme) {
                AppRoot()
            }
        }
    }
}
