package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass styling modifier for translucent optical-glass surfaces.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    isDark: Boolean = true,
    glassIntensity: Float = 0.85f,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = if (isDark) Color(0x33000000) else Color(0x1A000000),
        spotColor = if (isDark) Color(0x4D00F0FF) else Color(0x260284C7)
    )
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = if (isDark) {
                listOf(
                    Color(0xFF1E2638).copy(alpha = 0.65f * glassIntensity),
                    Color(0xFF0F1522).copy(alpha = 0.85f * glassIntensity)
                )
            } else {
                listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.80f * glassIntensity),
                    Color(0xFFF1F5F9).copy(alpha = 0.65f * glassIntensity)
                )
            }
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            colors = if (isDark) {
                listOf(
                    Color(0x66FFFFFF),
                    Color(0x1A00F0FF),
                    Color(0x0DFFFFFF)
                )
            } else {
                listOf(
                    Color(0x99FFFFFF),
                    Color(0x330284C7),
                    Color(0x1A000000)
                )
            }
        ),
        shape = shape
    )

/**
 * Extra subtle floating glass capsule modifier for buttons and navigation pills.
 */
fun Modifier.glassCapsule(
    isDark: Boolean = true,
    accentColor: Color = Color(0xFF00F0FF),
    isSelected: Boolean = false
): Modifier = this
    .clip(CircleShape)
    .background(
        brush = if (isSelected) {
            Brush.horizontalGradient(
                listOf(
                    accentColor.copy(alpha = if (isDark) 0.35f else 0.25f),
                    accentColor.copy(alpha = if (isDark) 0.15f else 0.10f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    if (isDark) Color(0x2EFFFFFF) else Color(0x80FFFFFF),
                    if (isDark) Color(0x0DFFFFFF) else Color(0x40FFFFFF)
                )
            )
        }
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            listOf(
                if (isSelected) accentColor.copy(alpha = 0.8f) else (if (isDark) Color(0x4DFFFFFF) else Color(0x80FFFFFF)),
                if (isSelected) accentColor.copy(alpha = 0.2f) else Color(0x10FFFFFF)
            )
        ),
        shape = CircleShape
    )
