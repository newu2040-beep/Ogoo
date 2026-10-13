package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ColorGradingWheel(
    title: String,
    hue: Float,
    saturation: Float,
    onColorChanged: (hue: Float, sat: Float) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (isDark) Color.White else Color(0xFF1E293B),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .testTag("color_wheel_${title.lowercase()}")
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val pos = change.position
                            val dx = pos.x - center.x
                            val dy = pos.y - center.y
                            val radius = size.width / 2f

                            val dist = sqrt(dx * dx + dy * dy)
                            val sat = (dist / radius).coerceIn(0f, 1f) * 100f
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            onColorChanged(angle, sat)
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 4.dp.toPx()

                // Wheel disc background
                drawCircle(
                    color = if (isDark) Color(0xFF1A2234) else Color(0xFFE2E8F0),
                    radius = radius,
                    center = center
                )

                // Outer chromatic ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color.Magenta,
                            Color.Red
                        ),
                        center = center
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 6.dp.toPx())
                )

                // Puck handle position based on hue & saturation
                val rad = Math.toRadians(hue.toDouble())
                val puckDist = (saturation / 100f) * (radius - 8.dp.toPx())
                val puckX = center.x + (cos(rad) * puckDist).toFloat()
                val puckY = center.y + (sin(rad) * puckDist).toFloat()

                // Draw puck
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(puckX, puckY)
                )
                drawCircle(
                    color = if (saturation > 0) Color.hsv(hue, (saturation / 100f).coerceIn(0f, 1f), 1f) else Color.Gray,
                    radius = 5.dp.toPx(),
                    center = Offset(puckX, puckY)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (saturation > 0f) "${hue.toInt()}° / ${saturation.toInt()}%" else "Neutral",
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontSize = 10.sp
        )
    }
}
