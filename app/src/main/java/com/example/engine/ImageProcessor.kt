package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.example.data.lut.CubeLutData
import com.example.data.model.EditRecipe
import com.example.data.model.HistogramData
import com.example.data.model.PresetCatalog
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object ImageProcessor {

    /**
     * Applies full non-destructive editing pipeline to input Bitmap.
     */
    fun processBitmap(
        sourceBitmap: Bitmap,
        recipe: EditRecipe,
        cubeLut: CubeLutData? = null
    ): Bitmap {
        // Step 1: Geometry & Orientation
        var currentBitmap = applyTransform(sourceBitmap, recipe)

        val width = currentBitmap.width
        val height = currentBitmap.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        currentBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Precompute lookup tables and constants for speed
        val evMultiplier = 2.0.pow(recipe.exposure.toDouble()).toFloat()
        val contrastFactor = (259.0f * (recipe.contrast + 100.0f)) / (100.0f * (259.0f - recipe.contrast))
        val satMultiplier = (recipe.saturation + 100f) / 100f
        val vibMultiplier = recipe.vibrance / 100f

        // White balance
        val tempShift = recipe.temperature / 100f
        val tintShift = recipe.tint / 100f
        val rTemp = (1.0f + tempShift * 0.35f).coerceAtLeast(0.1f)
        val bTemp = (1.0f - tempShift * 0.35f).coerceAtLeast(0.1f)
        val gTint = (1.0f - tintShift * 0.25f).coerceAtLeast(0.1f)
        val rTint = (1.0f + tintShift * 0.15f).coerceAtLeast(0.1f)
        val bTint = (1.0f + tintShift * 0.15f).coerceAtLeast(0.1f)

        // Highlights & Shadows
        val highlightsAdj = recipe.highlights / 100f
        val shadowsAdj = recipe.shadows / 100f
        val whitesAdj = recipe.whites / 100f
        val blacksAdj = recipe.blacks / 100f

        // Tone Curve LUTs
        val masterLut = CurveEvaluator.buildLut(recipe.curvePointsMaster)
        val redLut = CurveEvaluator.buildLut(recipe.curvePointsRed)
        val greenLut = CurveEvaluator.buildLut(recipe.curvePointsGreen)
        val blueLut = CurveEvaluator.buildLut(recipe.curvePointsBlue)

        // Active LUT preset
        val preset = PresetCatalog.getPresetById(recipe.lutId)
        val lutIntensity = recipe.lutIntensity.coerceIn(0f, 1f)

        // Precalculate 3-Way Color Grading shifts
        val hasGrading = (recipe.shadowsSat > 0 || recipe.midtonesSat > 0 || recipe.highlightsSat > 0)
        val (shR, shG, shB) = colorWheelOffset(recipe.shadowsHue, recipe.shadowsSat)
        val (midR, midG, midB) = colorWheelOffset(recipe.midtonesHue, recipe.midtonesSat)
        val (hiR, hiG, hiB) = colorWheelOffset(recipe.highlightsHue, recipe.highlightsSat)

        // Check HSL adjustments
        val hasHsl = recipe.hslHues.any { it != 0f } ||
                recipe.hslSaturations.any { it != 0f } ||
                recipe.hslLuminances.any { it != 0f }

        val hsv = FloatArray(3)

        // Center coordinates for vignette
        val centerX = width / 2f
        val centerY = height / 2f
        val maxDist = sqrt(centerX * centerX + centerY * centerY)
        val hasVignette = recipe.vignetteAmount != 0f || (preset?.vignetteShift ?: 0f) != 0f
        val vignetteStrength = (recipe.vignetteAmount + (preset?.vignetteShift ?: 0f)) / 100f
        val feather = (recipe.vignetteFeather / 100f).coerceIn(0.1f, 1.0f)

        // Film grain
        val hasGrain = recipe.filmGrainAmount > 0f || (preset?.grainShift ?: 0f) > 0f
        val grainAmount = (recipe.filmGrainAmount + (preset?.grainShift ?: 0f)).coerceAtLeast(0f) / 100f

        // Main Pixel Processing Loop
        for (i in 0 until totalPixels) {
            val color = pixels[i]
            val a = (color ushr 24) and 0xFF
            var r = (color ushr 16) and 0xFF
            var g = (color ushr 8) and 0xFF
            var b = color and 0xFF

            // 1. Exposure
            var rF = r * evMultiplier
            var gF = g * evMultiplier
            var bF = b * evMultiplier

            // 2. White Balance
            rF *= (rTemp * rTint)
            gF *= gTint
            bF *= (bTemp * bTint)

            // 3. Highlights & Shadows & Whites & Blacks
            var lum = (0.299f * rF + 0.587f * gF + 0.114f * bF) / 255f
            if (lum > 0.5f && highlightsAdj != 0f) {
                val weight = (lum - 0.5f) * 2f
                val factor = 1f + highlightsAdj * weight * 0.4f
                rF *= factor
                gF *= factor
                bF *= factor
            } else if (lum <= 0.5f && shadowsAdj != 0f) {
                val weight = (0.5f - lum) * 2f
                val factor = 1f + shadowsAdj * weight * 0.5f
                rF *= factor
                gF *= factor
                bF *= factor
            }

            if (whitesAdj != 0f && lum > 0.7f) {
                val weight = (lum - 0.7f) / 0.3f
                rF += whitesAdj * weight * 30f
                gF += whitesAdj * weight * 30f
                bF += whitesAdj * weight * 30f
            }
            if (blacksAdj != 0f && lum < 0.3f) {
                val weight = (0.3f - lum) / 0.3f
                rF += blacksAdj * weight * 25f
                gF += blacksAdj * weight * 25f
                bF += blacksAdj * weight * 25f
            }

            // 4. Contrast
            rF = (contrastFactor * (rF - 128f) + 128f)
            gF = (contrastFactor * (gF - 128f) + 128f)
            bF = (contrastFactor * (bF - 128f) + 128f)

            // 5. Curves
            var rInt = rF.toInt().coerceIn(0, 255)
            var gInt = gF.toInt().coerceIn(0, 255)
            var bInt = bF.toInt().coerceIn(0, 255)

            rInt = redLut[masterLut[rInt]]
            gInt = greenLut[masterLut[gInt]]
            bInt = blueLut[masterLut[bInt]]

            rF = rInt.toFloat()
            gF = gInt.toFloat()
            bF = bInt.toFloat()

            // 6. Saturation & Vibrance
            val maxC = max(rF, max(gF, bF))
            val minC = min(rF, min(gF, bF))
            lum = (0.299f * rF + 0.587f * gF + 0.114f * bF)

            if (vibMultiplier != 0f) {
                val currentSat = if (maxC == 0f) 0f else (maxC - minC) / maxC
                val vibFactor = 1f + vibMultiplier * (1f - currentSat)
                rF = lum + (rF - lum) * vibFactor
                gF = lum + (gF - lum) * vibFactor
                bF = lum + (bF - lum) * vibFactor
            }

            if (satMultiplier != 1.0f) {
                rF = lum + (rF - lum) * satMultiplier
                gF = lum + (gF - lum) * satMultiplier
                bF = lum + (bF - lum) * satMultiplier
            }

            // 7. HSL Color Mixer
            if (hasHsl) {
                Color.RGBToHSV(
                    rF.toInt().coerceIn(0, 255),
                    gF.toInt().coerceIn(0, 255),
                    bF.toInt().coerceIn(0, 255),
                    hsv
                )
                val hue = hsv[0]
                val channelIndex = getHslChannel(hue)
                val hShift = recipe.hslHues[channelIndex]
                val sShift = recipe.hslSaturations[channelIndex] / 100f
                val lShift = recipe.hslLuminances[channelIndex] / 100f

                hsv[0] = (hsv[0] + hShift).mod(360f)
                hsv[1] = (hsv[1] * (1f + sShift)).coerceIn(0f, 1f)
                hsv[2] = (hsv[2] * (1f + lShift)).coerceIn(0f, 1f)

                val rgbHsl = Color.HSVToColor(hsv)
                rF = ((rgbHsl ushr 16) and 0xFF).toFloat()
                gF = ((rgbHsl ushr 8) and 0xFF).toFloat()
                bF = (rgbHsl and 0xFF).toFloat()
            }

            // 8. 3-Way Color Grading Wheels
            if (hasGrading) {
                val normLum = (0.299f * rF + 0.587f * gF + 0.114f * bF) / 255f
                val shWeight = (1f - normLum * 2f).coerceIn(0f, 1f)
                val hiWeight = (normLum * 2f - 1f).coerceIn(0f, 1f)
                val midWeight = (1f - shWeight - hiWeight).coerceIn(0f, 1f)

                rF += (shR * shWeight + midR * midWeight + hiR * hiWeight) * 40f
                gF += (shG * shWeight + midG * midWeight + hiG * hiWeight) * 40f
                bF += (shB * shWeight + midB * midWeight + hiB * hiWeight) * 40f
            }

            // 9. LUT Preset Application
            if (preset != null && lutIntensity > 0f) {
                var targetR = rF
                var targetG = gF
                var targetB = bF

                if (preset.isMonochrome) {
                    val mono = (0.299f * targetR + 0.587f * targetG + 0.114f * targetB)
                    targetR = mono
                    targetG = mono
                    targetB = mono
                }

                // Channel multipliers
                targetR *= preset.redScale
                targetG *= preset.greenScale
                targetB *= preset.blueScale

                // Tint shifts
                targetR *= (preset.highlightTintR * (targetR / 255f) + preset.shadowTintR * (1f - targetR / 255f))
                targetG *= (preset.highlightTintG * (targetG / 255f) + preset.shadowTintG * (1f - targetG / 255f))
                targetB *= (preset.highlightTintB * (targetB / 255f) + preset.shadowTintB * (1f - targetB / 255f))

                // Tone shifts
                targetR = (targetR + preset.contrastShift * 0.4f + preset.tempShift * 0.6f)
                targetG = (targetG + preset.contrastShift * 0.4f + preset.tintShift * 0.5f)
                targetB = (targetB + preset.contrastShift * 0.4f - preset.tempShift * 0.6f)

                rF = rF * (1f - lutIntensity) + targetR * lutIntensity
                gF = gF * (1f - lutIntensity) + targetG * lutIntensity
                bF = bF * (1f - lutIntensity) + targetB * lutIntensity
            }

            // Custom .cube 3D LUT sampling if provided
            if (cubeLut != null && lutIntensity > 0f) {
                val (lutR, lutG, lutB) = cubeLut.sample3D(
                    (rF / 255f).coerceIn(0f, 1f),
                    (gF / 255f).coerceIn(0f, 1f),
                    (bF / 255f).coerceIn(0f, 1f)
                )
                rF = rF * (1f - lutIntensity) + (lutR * 255f) * lutIntensity
                gF = gF * (1f - lutIntensity) + (lutG * 255f) * lutIntensity
                bF = bF * (1f - lutIntensity) + (lutB * 255f) * lutIntensity
            }

            // 10. Vignette
            if (hasVignette) {
                val px = i % width
                val py = i / width
                val dx = px - centerX
                val dy = py - centerY
                val dist = sqrt(dx * dx + dy * dy) / maxDist
                val vigFactor = (1f + vignetteStrength * (dist / feather).pow(2.0f).coerceIn(0f, 1f))
                rF *= vigFactor
                gF *= vigFactor
                bF *= vigFactor
            }

            // 11. Film Grain
            if (hasGrain) {
                // Pseudo-random noise seeded with pixel position
                val noise = ((sin(i.toDouble() * 12.9898) * 43758.5453).mod(1.0) - 0.5).toFloat()
                val grainVal = noise * grainAmount * 45f
                rF += grainVal
                gF += grainVal
                bF += grainVal
            }

            pixels[i] = (a shl 24) or
                    (rF.toInt().coerceIn(0, 255) shl 16) or
                    (gF.toInt().coerceIn(0, 255) shl 8) or
                    bF.toInt().coerceIn(0, 255)
        }

        // Apply Sharpening if needed
        if (recipe.sharpening > 0f) {
            applySharpening(pixels, width, height, recipe.sharpening / 100f)
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        if (currentBitmap != sourceBitmap) {
            currentBitmap.recycle()
        }

        return resultBitmap
    }

    /**
     * Maps Hue angle to 8 HSL channels:
     * 0: Red, 1: Orange, 2: Yellow, 3: Green, 4: Aqua, 5: Blue, 6: Purple, 7: Magenta
     */
    private fun getHslChannel(hue: Float): Int {
        return when (hue) {
            in 345f..360f, in 0f..15f -> 0 // Red
            in 15f..45f -> 1               // Orange
            in 45f..75f -> 2               // Yellow
            in 75f..150f -> 3              // Green
            in 150f..210f -> 4             // Aqua
            in 210f..265f -> 5             // Blue
            in 265f..310f -> 6             // Purple
            else -> 7                      // Magenta
        }
    }

    /**
     * Converts hue and saturation from color wheel into RGB tint offsets [-1..1].
     */
    private fun colorWheelOffset(hue: Float, sat: Float): Triple<Float, Float, Float> {
        if (sat <= 0f) return Triple(0f, 0f, 0f)
        val rad = Math.toRadians(hue.toDouble())
        val strength = (sat / 100f).coerceIn(0f, 1f)
        val r = (cos(rad) * strength).toFloat()
        val g = (cos(rad - 2.0944) * strength).toFloat() // 120 deg
        val b = (cos(rad - 4.1888) * strength).toFloat() // 240 deg
        return Triple(r, g, b)
    }

    /**
     * Unsharp mask sharpening kernel.
     */
    private fun applySharpening(pixels: IntArray, width: Int, height: Int, amount: Float) {
        val original = pixels.clone()
        val kernelWeight = amount * 0.8f
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val center = original[idx]
                val top = original[idx - width]
                val bottom = original[idx + width]
                val left = original[idx - 1]
                val right = original[idx + 1]

                val cr = (center ushr 16) and 0xFF
                val cg = (center ushr 8) and 0xFF
                val cb = center and 0xFF

                val avgR = (((top ushr 16) and 0xFF) + ((bottom ushr 16) and 0xFF) +
                        ((left ushr 16) and 0xFF) + ((right ushr 16) and 0xFF)) / 4f
                val avgG = (((top ushr 8) and 0xFF) + ((bottom ushr 8) and 0xFF) +
                        ((left ushr 8) and 0xFF) + ((right ushr 8) and 0xFF)) / 4f
                val avgB = ((top and 0xFF) + (bottom and 0xFF) +
                        (left and 0xFF) + (right and 0xFF)) / 4f

                val newR = (cr + (cr - avgR) * kernelWeight).toInt().coerceIn(0, 255)
                val newG = (cg + (cg - avgG) * kernelWeight).toInt().coerceIn(0, 255)
                val newB = (cb + (cb - avgB) * kernelWeight).toInt().coerceIn(0, 255)

                pixels[idx] = (center and -0x1000000) or (newR shl 16) or (newG shl 8) or newB
            }
        }
    }

    /**
     * Computes real 256-bin RGB and Luminance histogram from bitmap.
     */
    fun computeHistogram(bitmap: Bitmap): HistogramData {
        val w = bitmap.width
        val h = bitmap.height
        val step = max(1, (w * h) / 15000) // Sample up to 15,000 pixels for 60fps performance
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val rCounts = IntArray(256)
        val gCounts = IntArray(256)
        val bCounts = IntArray(256)
        val lumCounts = IntArray(256)

        var sampled = 0
        for (i in 0 until pixels.size step step) {
            val c = pixels[i]
            val r = (c ushr 16) and 0xFF
            val g = (c ushr 8) and 0xFF
            val b = c and 0xFF
            val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)

            rCounts[r]++
            gCounts[g]++
            bCounts[b]++
            lumCounts[lum]++
            sampled++
        }

        val maxVal = max(
            rCounts.maxOrNull() ?: 1,
            max(gCounts.maxOrNull() ?: 1, max(bCounts.maxOrNull() ?: 1, lumCounts.maxOrNull() ?: 1))
        ).toFloat().coerceAtLeast(1f)

        val rNorm = FloatArray(256) { rCounts[it] / maxVal }
        val gNorm = FloatArray(256) { gCounts[it] / maxVal }
        val bNorm = FloatArray(256) { bCounts[it] / maxVal }
        val lumNorm = FloatArray(256) { lumCounts[it] / maxVal }

        return HistogramData(rNorm, gNorm, bNorm, lumNorm)
    }

    /**
     * Calculates real auto-tone adjustments based on pixel statistics.
     */
    fun computeAutoTone(bitmap: Bitmap): EditRecipe {
        val w = bitmap.width
        val h = bitmap.height
        val step = max(1, (w * h) / 10000)
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        var totalLum = 0.0
        var count = 0
        var minLum = 255
        var maxLum = 0

        for (i in 0 until pixels.size step step) {
            val c = pixels[i]
            val r = (c ushr 16) and 0xFF
            val g = (c ushr 8) and 0xFF
            val b = c and 0xFF
            val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            totalLum += lum
            if (lum < minLum) minLum = lum
            if (lum > maxLum) maxLum = lum
            count++
        }

        val avgLum = if (count > 0) totalLum / count else 128.0
        val targetLum = 128.0
        val exposureAdj = ((targetLum - avgLum) / 128.0 * 0.8).toFloat().coerceIn(-1.5f, 1.5f)

        val dynamicRange = maxLum - minLum
        val contrastAdj = if (dynamicRange < 180) {
            ((180 - dynamicRange) * 0.4f).coerceIn(0f, 40f)
        } else {
            -((dynamicRange - 220) * 0.2f).coerceIn(-20f, 0f)
        }

        val shadowsAdj = if (minLum < 20) 25f else 10f
        val highlightsAdj = if (maxLum > 240) -20f else -5f

        return EditRecipe(
            exposure = exposureAdj,
            contrast = contrastAdj,
            highlights = highlightsAdj,
            shadows = shadowsAdj,
            vibrance = 15f
        )
    }

    /**
     * Applies crop, rotation, and aspect ratio transformation.
     */
    fun applyTransform(source: Bitmap, recipe: EditRecipe): Bitmap {
        var bmp = source
        val matrix = Matrix()
        var needMatrix = false

        if (recipe.rotationDegrees != 0f) {
            matrix.postRotate(recipe.rotationDegrees)
            needMatrix = true
        }

        if (recipe.flipHorizontal || recipe.flipVertical) {
            val sx = if (recipe.flipHorizontal) -1f else 1f
            val sy = if (recipe.flipVertical) -1f else 1f
            matrix.postScale(sx, sy)
            needMatrix = true
        }

        if (needMatrix) {
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        }

        // Apply crop aspect ratio if not Original
        if (recipe.cropAspect != "Original") {
            val targetRatio = when (recipe.cropAspect) {
                "1:1" -> 1f / 1f
                "4:5" -> 4f / 5f
                "16:9" -> 16f / 9f
                "9:16" -> 9f / 16f
                "3:2" -> 3f / 2f
                else -> 1f
            }

            val currentRatio = bmp.width.toFloat() / bmp.height.toFloat()
            var cropW = bmp.width
            var cropH = bmp.height

            if (currentRatio > targetRatio) {
                // Too wide, crop width
                cropW = (bmp.height * targetRatio).toInt().coerceIn(1, bmp.width)
            } else {
                // Too tall, crop height
                cropH = (bmp.width / targetRatio).toInt().coerceIn(1, bmp.height)
            }

            val startX = (bmp.width - cropW) / 2
            val startY = (bmp.height - cropH) / 2

            val cropped = Bitmap.createBitmap(bmp, startX, startY, cropW, cropH)
            if (bmp != source && bmp != cropped) {
                bmp.recycle()
            }
            bmp = cropped
        }

        return bmp
    }
}
