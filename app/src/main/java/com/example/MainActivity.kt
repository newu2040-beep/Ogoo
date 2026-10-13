package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.model.ThemePreferences
import com.example.data.repository.SettingsRepository
import com.example.ui.navigation.OgooNavHost
import com.example.ui.theme.OgooTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsRepository = remember { SettingsRepository(applicationContext) }
            val preferences by settingsRepository.themePreferences.collectAsState(initial = ThemePreferences())

            OgooTheme(preferences = preferences) {
                OgooNavHost(
                    preferences = preferences,
                    settingsRepository = settingsRepository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
