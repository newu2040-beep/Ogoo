package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.liquidGlass

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    modifier: Modifier = Modifier,
    isSplitMode: Boolean = true
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isSplitMode) {
                if (isSplitMode) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newFraction = (splitFraction + dragAmount.x / size.width).coerceIn(0.05f, 0.95f)
                        splitFraction = newFraction
                    }
                }
            }
            .testTag("before_after_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Calculate scaled image size preserving aspect ratio
            val imgW = afterBitmap.width.toFloat()
            val imgH = afterBitmap.height.toFloat()
            val scale = minOf(canvasW / imgW, canvasH / imgH)
            val drawW = (imgW * scale).toInt()
            val drawH = (imgH * scale).toInt()
            val leftOffset = ((canvasW - drawW) / 2f).toInt()
            val topOffset = ((canvasH - drawH) / 2f).toInt()

            val dstOffset = IntOffset(leftOffset, topOffset)
            val dstSize = IntSize(drawW, drawH)

            if (!isSplitMode) {
                // Show processed bitmap directly
                drawImage(
                    image = afterBitmap.asImageBitmap(),
                    dstOffset = dstOffset,
                    dstSize = dstSize
                )
            } else {
                val splitX = canvasW * splitFraction

                // Left: Original (Before)
                clipRect(left = 0f, top = 0f, right = splitX, bottom = canvasH) {
                    drawImage(
                        image = beforeBitmap.asImageBitmap(),
                        dstOffset = dstOffset,
                        dstSize = dstSize
                    )
                }

                // Right: Processed (After)
                clipRect(left = splitX, top = 0f, right = canvasW, bottom = canvasH) {
                    drawImage(
                        image = afterBitmap.asImageBitmap(),
                        dstOffset = dstOffset,
                        dstSize = dstSize
                    )
                }

                // Split divider line
                drawLine(
                    color = Color.White,
                    start = Offset(splitX, 0f),
                    end = Offset(splitX, canvasH),
                    strokeWidth = 2.dp.toPx()
                )

                // Divider circle handle
                drawCircle(
                    color = Color.White,
                    radius = 16.dp.toPx(),
                    center = Offset(splitX, canvasH / 2f)
                )
                drawCircle(
                    color = Color(0xFF00F0FF),
                    radius = 12.dp.toPx(),
                    center = Offset(splitX, canvasH / 2f)
                )
            }
        }

        if (isSplitMode) {
            // Badges for BEFORE / AFTER
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .liquidGlass(shape = CircleShape, isDark = true, glassIntensity = 0.9f)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ORIGINAL",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .liquidGlass(shape = CircleShape, isDark = true, glassIntensity = 0.9f)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "EDITED",
                    color = Color(0xFF00F0FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
