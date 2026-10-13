package com.example.data.lut

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class CubeLutData(
    val title: String,
    val size: Int,
    val is3D: Boolean,
    // For 3D LUT: array of floats of size (size * size * size * 3)
    val table: FloatArray
) {
    /**
     * Samples the 3D LUT with trilinear interpolation.
     * r, g, b in [0.0f, 1.0f].
     * Returns Triple(outR, outG, outB) in [0.0f, 1.0f].
     */
    fun sample3D(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
        val maxIndex = (size - 1).toFloat()
        val rClamped = r.coerceIn(0f, 1f) * maxIndex
        val gClamped = g.coerceIn(0f, 1f) * maxIndex
        val bClamped = b.coerceIn(0f, 1f) * maxIndex

        val r0 = rClamped.toInt().coerceIn(0, size - 2)
        val r1 = r0 + 1
        val g0 = gClamped.toInt().coerceIn(0, size - 2)
        val g1 = g0 + 1
        val b0 = bClamped.toInt().coerceIn(0, size - 2)
        val b1 = b0 + 1

        val fr = rClamped - r0
        val fg = gClamped - g0
        val fb = bClamped - b0

        fun getLut(ir: Int, ig: Int, ib: Int): Triple<Float, Float, Float> {
            val idx = (ir + ig * size + ib * size * size) * 3
            if (idx + 2 < table.size) {
                return Triple(table[idx], table[idx + 1], table[idx + 2])
            }
            return Triple(ir.toFloat() / maxIndex, ig.toFloat() / maxIndex, ib.toFloat() / maxIndex)
        }

        val c000 = getLut(r0, g0, b0)
        val c100 = getLut(r1, g0, b0)
        val c010 = getLut(r0, g1, b0)
        val c110 = getLut(r1, g1, b0)
        val c001 = getLut(r0, g0, b1)
        val c101 = getLut(r1, g0, b1)
        val c011 = getLut(r0, g1, b1)
        val c111 = getLut(r1, g1, b1)

        fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

        val c00 = lerp(c000.first, c100.first, fr)
        val c10 = lerp(c010.first, c110.first, fr)
        val c01 = lerp(c001.first, c101.first, fr)
        val c11 = lerp(c011.first, c111.first, fr)

        val c0 = lerp(c00, c10, fg)
        val c1 = lerp(c01, c11, fg)
        val outR = lerp(c0, c1, fb)

        val g00 = lerp(c000.second, c100.second, fr)
        val g10 = lerp(c010.second, c110.second, fr)
        val g01 = lerp(c001.second, c101.second, fr)
        val g11 = lerp(c011.second, c111.second, fr)

        val g0_ = lerp(g00, g10, fg)
        val g1_ = lerp(g01, g11, fg)
        val outG = lerp(g0_, g1_, fb)

        val b00 = lerp(c000.third, c100.third, fr)
        val b10 = lerp(c010.third, c110.third, fr)
        val b01 = lerp(c001.third, c101.third, fr)
        val b11 = lerp(c011.third, c111.third, fr)

        val b0_ = lerp(b00, b10, fg)
        val b1_ = lerp(b01, b11, fg)
        val outB = lerp(b0_, b1_, fb)

        return Triple(outR.coerceIn(0f, 1f), outG.coerceIn(0f, 1f), outB.coerceIn(0f, 1f))
    }
}

object CubeLutParser {

    /**
     * Parses a standard Adobe / DaVinci .cube file from an InputStream.
     */
    fun parse(inputStream: InputStream): CubeLutData? {
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            var title = "Imported LUT"
            var size = 0
            var is3D = true
            val dataPoints = mutableListOf<Float>()

            reader.forEachLine { rawLine ->
                val line = rawLine.trim()
                if (line.isNotEmpty() && !line.startsWith("#")) {
                    val upper = line.uppercase()
                    when {
                        upper.startsWith("TITLE") -> {
                            val parts = line.split(Regex("[\"\\s]+"), 2)
                            if (parts.size > 1) {
                                title = parts[1].replace("\"", "").trim()
                            }
                        }
                        upper.startsWith("LUT_3D_SIZE") -> {
                            is3D = true
                            val parts = line.split(Regex("\\s+"))
                            if (parts.size > 1) {
                                size = parts[1].toIntOrNull() ?: 17
                            }
                        }
                        upper.startsWith("LUT_1D_SIZE") -> {
                            is3D = false
                            val parts = line.split(Regex("\\s+"))
                            if (parts.size > 1) {
                                size = parts[1].toIntOrNull() ?: 256
                            }
                        }
                        else -> {
                            // Data line with r g b floats
                            val tokens = line.split(Regex("\\s+"))
                            if (tokens.size >= 3) {
                                val r = tokens[0].toFloatOrNull()
                                val g = tokens[1].toFloatOrNull()
                                val b = tokens[2].toFloatOrNull()
                                if (r != null && g != null && b != null) {
                                    dataPoints.add(r)
                                    dataPoints.add(g)
                                    dataPoints.add(b)
                                }
                            }
                        }
                    }
                }
            }

            if (size <= 0) {
                // Infer from points
                val totalColors = dataPoints.size / 3
                size = Math.cbrt(totalColors.toDouble()).toInt()
                if (size * size * size != totalColors) size = 17
            }

            if (dataPoints.isEmpty()) null
            else CubeLutData(
                title = title,
                size = size,
                is3D = is3D,
                table = dataPoints.toFloatArray()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
