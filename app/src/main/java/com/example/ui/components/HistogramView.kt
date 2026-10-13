package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.HistogramData

@Composable
fun HistogramView(
    data: HistogramData,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    GlassCard(
        modifier = modifier
            .width(130.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        isDark = isDark,
        glassIntensity = 0.8f
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            val w = size.width
            val h = size.height

            fun drawChannel(values: FloatArray, color: Color, strokeWidth: Float) {
                if (values.isEmpty()) return
                val path = Path()
                val stepX = w / 255f

                path.moveTo(0f, h - values[0].coerceIn(0f, 1f) * (h - 2f))
                for (i in 1..255) {
                    val x = i * stepX
                    val y = h - values[i].coerceIn(0f, 1f) * (h - 2f)
                    path.lineTo(x, y)
                }
                drawPath(path, color = color, style = Stroke(width = strokeWidth))
            }

            // Draw RGB and Luminance Curves
            drawChannel(data.lumChannel, Color(0x66FFFFFF), 1.0f)
            drawChannel(data.redChannel, Color(0xCCFF453A), 1.2f)
            drawChannel(data.greenChannel, Color(0xCC30D158), 1.2f)
            drawChannel(data.blueChannel, Color(0xCC0A84FF), 1.2f)
        }
    }
}
