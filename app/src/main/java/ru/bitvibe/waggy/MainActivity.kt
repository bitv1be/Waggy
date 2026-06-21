package ru.bitvibe.waggy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
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
            val isDarkTheme by themePreferences.isDarkMode.collectAsStateWithLifecycle()
            val useDarkTheme = isDarkTheme ?: isSystemInDarkTheme()

            WaggyTheme(darkTheme = useDarkTheme) {
                AppRoot()
            }
        }
    }
}