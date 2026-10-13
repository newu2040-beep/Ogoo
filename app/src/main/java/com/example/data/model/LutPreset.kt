package com.example.data.model

data class LutPreset(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val defaultIntensity: Float = 1.0f,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    // Characteristic mathematical color transformation parameters
    val tempShift: Float = 0f,
    val tintShift: Float = 0f,
    val contrastShift: Float = 0f,
    val highlightsShift: Float = 0f,
    val shadowsShift: Float = 0f,
    val saturationShift: Float = 0f,
    val vibranceShift: Float = 0f,
    val dehazeShift: Float = 0f,
    val vignetteShift: Float = 0f,
    val grainShift: Float = 0f,
    // Primary color balance / grading tint
    val shadowTintR: Float = 1.0f,
    val shadowTintG: Float = 1.0f,
    val shadowTintB: Float = 1.0f,
    val highlightTintR: Float = 1.0f,
    val highlightTintG: Float = 1.0f,
    val highlightTintB: Float = 1.0f,
    // Channel matrix multipliers (R, G, B scaling)
    val redScale: Float = 1.0f,
    val greenScale: Float = 1.0f,
    val blueScale: Float = 1.0f,
    // Monochrome flag
    val isMonochrome: Boolean = false
)
