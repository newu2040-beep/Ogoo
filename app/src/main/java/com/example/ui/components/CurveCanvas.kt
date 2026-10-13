package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.CurveEvaluator

@Composable
fun CurveCanvas(
    points: List<Pair<Float, Float>>,
    onPointsChanged: (List<Pair<Float, Float>>) -> Unit,
    curveColor: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    var draggingIndex by remember { mutableIntStateOf(-1) }

    GlassCard(
        modifier = modifier
            .width(260.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
        glassIntensity = 0.85f
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("curve_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val w = size.width
                                val h = size.height
                                val touchThreshold = 24.dp.toPx()
                                // Find nearest point
                                draggingIndex = points.indexOfFirst { pt ->
                                    val px = pt.first * w
                                    val py = (1f - pt.second) * h
                                    val dist = (px - offset.x) * (px - offset.x) + (py - offset.y) * (py - offset.y)
                                    dist <= touchThreshold * touchThreshold
                                }
                            },
                            onDrag = { change, _ ->
                                if (draggingIndex >= 0) {
                                    change.consume()
                                    val w = size.width
                                    val h = size.height
                                    val nx = (change.position.x / w).coerceIn(0f, 1f)
                                    val ny = (1f - change.position.y / h).coerceIn(0f, 1f)

                                    val mutable = points.toMutableList()
                                    // Keep endpoints bounded
                                    if (draggingIndex == 0) {
                                        mutable[0] = 0f to ny
                                    } else if (draggingIndex == points.size - 1) {
                                        mutable[points.size - 1] = 1f to ny
                                    } else {
                                        mutable[draggingIndex] = nx to ny
                                    }
                                    onPointsChanged(mutable.sortedBy { it.first })
                                }
                            },
                            onDragEnd = { draggingIndex = -1 },
                            onDragCancel = { draggingIndex = -1 }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw Grid (quarters)
                val gridColor = if (isDark) Color(0x1FFFFFFF) else Color(0x1F000000)
                for (i in 1..3) {
                    val lineX = w * (i / 4f)
                    val lineY = h * (i / 4f)
                    drawLine(gridColor, Offset(lineX, 0f), Offset(lineX, h), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, lineY), Offset(w, lineY), strokeWidth = 1f)
                }

                // Diagonal base reference
                drawLine(
                    color = if (isDark) Color(0x33FFFFFF) else Color(0x33000000),
                    start = Offset(0f, h),
                    end = Offset(w, 0f),
                    strokeWidth = 1.dp.toPx()
                )

                // Build smooth spline LUT and plot
                val lut = CurveEvaluator.buildLut(points)
                val curvePath = Path()
                curvePath.moveTo(0f, h - (lut[0] / 255f) * h)
                for (i in 1..255) {
                    val x = (i / 255f) * w
                    val y = h - (lut[i] / 255f) * h
                    curvePath.lineTo(x, y)
                }

                drawPath(curvePath, color = curveColor, style = Stroke(width = 2.5.dp.toPx()))

                // Draw Points
                points.forEachIndexed { index, pt ->
                    val px = pt.first * w
                    val py = (1f - pt.second) * h
                    drawCircle(
                        color = Color.White,
                        radius = if (index == draggingIndex) 7.dp.toPx() else 5.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = curveColor,
                        radius = if (index == draggingIndex) 5.dp.toPx() else 3.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
