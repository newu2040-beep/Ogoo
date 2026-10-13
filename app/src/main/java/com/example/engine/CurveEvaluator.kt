package com.example.engine

object CurveEvaluator {

    /**
     * Builds a 256-entry lookup table for an input channel [0..255] -> [0..255]
     * using monotonic cubic interpolation between normalized control points (0..1).
     */
    fun buildLut(points: List<Pair<Float, Float>>): IntArray {
        val lut = IntArray(256)
        if (points.isEmpty()) {
            for (i in 0..255) lut[i] = i
            return lut
        }

        // Sort points by x coordinate
        val sorted = points.sortedBy { it.first }.toMutableList()
        if (sorted.first().first > 0f) {
            sorted.add(0, 0f to sorted.first().second)
        }
        if (sorted.last().first < 1f) {
            sorted.add(1f to sorted.last().second)
        }

        val n = sorted.size
        val x = DoubleArray(n) { sorted[it].first.toDouble() * 255.0 }
        val y = DoubleArray(n) { sorted[it].second.toDouble() * 255.0 }

        // Compute slopes
        val m = DoubleArray(n)
        for (i in 0 until n - 1) {
            val dx = x[i + 1] - x[i]
            if (dx > 0) {
                m[i] = (y[i + 1] - y[i]) / dx
            }
        }
        m[n - 1] = m[n - 2]

        // Tangents
        val d = DoubleArray(n)
        d[0] = m[0]
        d[n - 1] = m[n - 2]
        for (i in 1 until n - 1) {
            d[i] = (m[i - 1] + m[i]) * 0.5
        }

        // Interpolate for all 0..255
        var seg = 0
        for (i in 0..255) {
            val xi = i.toDouble()
            while (seg < n - 2 && xi > x[seg + 1]) {
                seg++
            }
            val h = x[seg + 1] - x[seg]
            if (h <= 0.0) {
                lut[i] = y[seg].toInt().coerceIn(0, 255)
            } else {
                val t = (xi - x[seg]) / h
                val t2 = t * t
                val t3 = t2 * t
                val h00 = 2 * t3 - 3 * t2 + 1
                val h10 = t3 - 2 * t2 + t
                val h01 = -2 * t3 + 3 * t2
                val h11 = t3 - t2
                val yi = h00 * y[seg] + h10 * h * d[seg] + h01 * y[seg + 1] + h11 * h * d[seg + 1]
                lut[i] = yi.toInt().coerceIn(0, 255)
            }
        }

        return lut
    }
}
