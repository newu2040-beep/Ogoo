package com.example.data.model

enum class AppTheme(val title: String, val description: String) {
    OBSIDIAN("Obsidian Glass", "Graphite surfaces with cool cyan accents"),
    CLOUD("Cloud Glass", "Warm white translucent surfaces with pale blue"),
    MINT("Mint Glass", "Sage, mint and emerald luminous highlights"),
    AURORA("Aurora Glass", "Lavender, peach and pastel violet gradients"),
    ROSE("Rose Glass", "Muted rose, blush and warm neutral glass"),
    CUSTOM("Custom Glass", "Personalized accent and glass intensity")
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class ThemePreferences(
    val theme: AppTheme = AppTheme.OBSIDIAN,
    val mode: ThemeMode = ThemeMode.DARK,
    val glassIntensity: Float = 0.85f, // 0.0 to 1.0
    val hapticEnabled: Boolean = true,
    val customAccentColor: Long = 0xFF00F0FFL, // Default cyan
    val compactMode: Boolean = false,
    val autoSaveToGallery: Boolean = true
)
