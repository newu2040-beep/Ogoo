package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Non-destructive edit recipe containing all image processing adjustments.
 */
data class EditRecipe(
    // Light
    val exposure: Float = 0f,        // -2.0 to +2.0 EV
    val contrast: Float = 0f,        // -100 to +100
    val highlights: Float = 0f,      // -100 to +100
    val shadows: Float = 0f,         // -100 to +100
    val whites: Float = 0f,          // -100 to +100
    val blacks: Float = 0f,          // -100 to +100

    // Color
    val temperature: Float = 0f,     // -100 (cool) to +100 (warm)
    val tint: Float = 0f,            // -100 (green) to +100 (magenta)
    val vibrance: Float = 0f,        // -100 to +100
    val saturation: Float = 0f,      // -100 to +100

    // Effects
    val texture: Float = 0f,         // 0 to 100
    val clarity: Float = 0f,         // -100 to +100
    val dehaze: Float = 0f,          // 0 to 100
    val vignetteAmount: Float = 0f,  // -100 to +100 (negative is dark vignette)
    val vignetteFeather: Float = 50f,// 0 to 100
    val filmGrainAmount: Float = 0f, // 0 to 100
    val filmGrainSize: Float = 25f,  // 0 to 100

    // Detail
    val sharpening: Float = 0f,      // 0 to 100
    val noiseReduction: Float = 0f,  // 0 to 100

    // HSL Adjustments: 8 channels (0:Red, 1:Orange, 2:Yellow, 3:Green, 4:Aqua, 5:Blue, 6:Purple, 7:Magenta)
    // Each has: hueShift (-180 to 180), saturationFactor (-100 to 100), luminanceFactor (-100 to 100)
    val hslHues: List<Float> = List(8) { 0f },
    val hslSaturations: List<Float> = List(8) { 0f },
    val hslLuminances: List<Float> = List(8) { 0f },

    // Curves: Normalized control points [(x, y)] for Master, Red, Green, Blue
    val curvePointsMaster: List<Pair<Float, Float>> = listOf(0f to 0f, 1f to 1f),
    val curvePointsRed: List<Pair<Float, Float>> = listOf(0f to 0f, 1f to 1f),
    val curvePointsGreen: List<Pair<Float, Float>> = listOf(0f to 0f, 1f to 1f),
    val curvePointsBlue: List<Pair<Float, Float>> = listOf(0f to 0f, 1f to 1f),

    // Color Grading Wheels (Hue in 0..360, Saturation in 0..100, Luminance in -100..100)
    val shadowsHue: Float = 0f,
    val shadowsSat: Float = 0f,
    val midtonesHue: Float = 0f,
    val midtonesSat: Float = 0f,
    val highlightsHue: Float = 0f,
    val highlightsSat: Float = 0f,

    // LUT Preset
    val lutId: String = "original",
    val lutIntensity: Float = 1.0f,  // 0.0 to 1.0

    // Geometry & Crop
    val rotationDegrees: Float = 0f, // 0, 90, 180, 270 or fine straightening
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cropAspect: String = "Original" // "Original", "1:1", "4:5", "16:9", "9:16", "3:2"
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("exposure", exposure.toDouble())
        obj.put("contrast", contrast.toDouble())
        obj.put("highlights", highlights.toDouble())
        obj.put("shadows", shadows.toDouble())
        obj.put("whites", whites.toDouble())
        obj.put("blacks", blacks.toDouble())
        obj.put("temperature", temperature.toDouble())
        obj.put("tint", tint.toDouble())
        obj.put("vibrance", vibrance.toDouble())
        obj.put("saturation", saturation.toDouble())
        obj.put("texture", texture.toDouble())
        obj.put("clarity", clarity.toDouble())
        obj.put("dehaze", dehaze.toDouble())
        obj.put("vignetteAmount", vignetteAmount.toDouble())
        obj.put("vignetteFeather", vignetteFeather.toDouble())
        obj.put("filmGrainAmount", filmGrainAmount.toDouble())
        obj.put("filmGrainSize", filmGrainSize.toDouble())
        obj.put("sharpening", sharpening.toDouble())
        obj.put("noiseReduction", noiseReduction.toDouble())

        val huesArr = JSONArray()
        hslHues.forEach { huesArr.put(it.toDouble()) }
        obj.put("hslHues", huesArr)

        val satsArr = JSONArray()
        hslSaturations.forEach { satsArr.put(it.toDouble()) }
        obj.put("hslSaturations", satsArr)

        val lumsArr = JSONArray()
        hslLuminances.forEach { lumsArr.put(it.toDouble()) }
        obj.put("hslLuminances", lumsArr)

        obj.put("shadowsHue", shadowsHue.toDouble())
        obj.put("shadowsSat", shadowsSat.toDouble())
        obj.put("midtonesHue", midtonesHue.toDouble())
        obj.put("midtonesSat", midtonesSat.toDouble())
        obj.put("highlightsHue", highlightsHue.toDouble())
        obj.put("highlightsSat", highlightsSat.toDouble())

        obj.put("lutId", lutId)
        obj.put("lutIntensity", lutIntensity.toDouble())
        obj.put("rotationDegrees", rotationDegrees.toDouble())
        obj.put("flipHorizontal", flipHorizontal)
        obj.put("flipVertical", flipVertical)
        obj.put("cropAspect", cropAspect)

        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): EditRecipe {
            if (jsonStr.isBlank()) return EditRecipe()
            return try {
                val obj = JSONObject(jsonStr)
                val huesList = mutableListOf<Float>()
                val huesArr = obj.optJSONArray("hslHues")
                if (huesArr != null) {
                    for (i in 0 until huesArr.length()) huesList.add(huesArr.getDouble(i).toFloat())
                } else {
                    repeat(8) { huesList.add(0f) }
                }

                val satsList = mutableListOf<Float>()
                val satsArr = obj.optJSONArray("hslSaturations")
                if (satsArr != null) {
                    for (i in 0 until satsArr.length()) satsList.add(satsArr.getDouble(i).toFloat())
                } else {
                    repeat(8) { satsList.add(0f) }
                }

                val lumsList = mutableListOf<Float>()
                val lumsArr = obj.optJSONArray("hslLuminances")
                if (lumsArr != null) {
                    for (i in 0 until lumsArr.length()) lumsList.add(lumsArr.getDouble(i).toFloat())
                } else {
                    repeat(8) { lumsList.add(0f) }
                }

                EditRecipe(
                    exposure = obj.optDouble("exposure", 0.0).toFloat(),
                    contrast = obj.optDouble("contrast", 0.0).toFloat(),
                    highlights = obj.optDouble("highlights", 0.0).toFloat(),
                    shadows = obj.optDouble("shadows", 0.0).toFloat(),
                    whites = obj.optDouble("whites", 0.0).toFloat(),
                    blacks = obj.optDouble("blacks", 0.0).toFloat(),
                    temperature = obj.optDouble("temperature", 0.0).toFloat(),
                    tint = obj.optDouble("tint", 0.0).toFloat(),
                    vibrance = obj.optDouble("vibrance", 0.0).toFloat(),
                    saturation = obj.optDouble("saturation", 0.0).toFloat(),
                    texture = obj.optDouble("texture", 0.0).toFloat(),
                    clarity = obj.optDouble("clarity", 0.0).toFloat(),
                    dehaze = obj.optDouble("dehaze", 0.0).toFloat(),
                    vignetteAmount = obj.optDouble("vignetteAmount", 0.0).toFloat(),
                    vignetteFeather = obj.optDouble("vignetteFeather", 50.0).toFloat(),
                    filmGrainAmount = obj.optDouble("filmGrainAmount", 0.0).toFloat(),
                    filmGrainSize = obj.optDouble("filmGrainSize", 25.0).toFloat(),
                    sharpening = obj.optDouble("sharpening", 0.0).toFloat(),
                    noiseReduction = obj.optDouble("noiseReduction", 0.0).toFloat(),
                    hslHues = huesList,
                    hslSaturations = satsList,
                    hslLuminances = lumsList,
                    shadowsHue = obj.optDouble("shadowsHue", 0.0).toFloat(),
                    shadowsSat = obj.optDouble("shadowsSat", 0.0).toFloat(),
                    midtonesHue = obj.optDouble("midtonesHue", 0.0).toFloat(),
                    midtonesSat = obj.optDouble("midtonesSat", 0.0).toFloat(),
                    highlightsHue = obj.optDouble("highlightsHue", 0.0).toFloat(),
                    highlightsSat = obj.optDouble("highlightsSat", 0.0).toFloat(),
                    lutId = obj.optString("lutId", "original"),
                    lutIntensity = obj.optDouble("lutIntensity", 1.0).toFloat(),
                    rotationDegrees = obj.optDouble("rotationDegrees", 0.0).toFloat(),
                    flipHorizontal = obj.optBoolean("flipHorizontal", false),
                    flipVertical = obj.optBoolean("flipVertical", false),
                    cropAspect = obj.optString("cropAspect", "Original")
                )
            } catch (e: Exception) {
                EditRecipe()
            }
        }
    }
}
