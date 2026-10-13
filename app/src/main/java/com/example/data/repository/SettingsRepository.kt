package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppTheme
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ogoo_settings")

class SettingsRepository(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("app_theme")
    private val KEY_MODE = stringPreferencesKey("theme_mode")
    private val KEY_GLASS_INTENSITY = floatPreferencesKey("glass_intensity")
    private val KEY_HAPTIC = booleanPreferencesKey("haptic_enabled")
    private val KEY_CUSTOM_ACCENT = longPreferencesKey("custom_accent")
    private val KEY_COMPACT_MODE = booleanPreferencesKey("compact_mode")
    private val KEY_AUTO_SAVE = booleanPreferencesKey("auto_save")

    val themePreferences: Flow<ThemePreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[KEY_THEME] ?: AppTheme.OBSIDIAN.name
        val modeStr = prefs[KEY_MODE] ?: ThemeMode.DARK.name
        val intensity = prefs[KEY_GLASS_INTENSITY] ?: 0.85f
        val haptic = prefs[KEY_HAPTIC] ?: true
        val accent = prefs[KEY_CUSTOM_ACCENT] ?: 0xFF00F0FFL
        val compact = prefs[KEY_COMPACT_MODE] ?: false
        val autoSave = prefs[KEY_AUTO_SAVE] ?: true

        ThemePreferences(
            theme = try { AppTheme.valueOf(themeStr) } catch (e: Exception) { AppTheme.OBSIDIAN },
            mode = try { ThemeMode.valueOf(modeStr) } catch (e: Exception) { ThemeMode.DARK },
            glassIntensity = intensity,
            hapticEnabled = haptic,
            customAccentColor = accent,
            compactMode = compact,
            autoSaveToGallery = autoSave
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun setMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MODE] = mode.name
        }
    }

    suspend fun toggleMode() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_MODE] ?: ThemeMode.DARK.name
            val next = if (current == ThemeMode.DARK.name) ThemeMode.LIGHT.name else ThemeMode.DARK.name
            prefs[KEY_MODE] = next
        }
    }

    suspend fun setGlassIntensity(intensity: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GLASS_INTENSITY] = intensity
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC] = enabled
        }
    }

    suspend fun setCustomAccent(color: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_ACCENT] = color
        }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_COMPACT_MODE] = enabled
        }
    }

    suspend fun setAutoSaveToGallery(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_SAVE] = enabled
        }
    }
}
