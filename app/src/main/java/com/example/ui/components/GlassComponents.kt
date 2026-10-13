package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.liquidGlass

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    isDark: Boolean = true,
    glassIntensity: Float = 0.85f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .liquidGlass(shape = shape, isDark = isDark, glassIntensity = glassIntensity)
            .then(clickModifier)
    ) {
        content()
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    isSelected: Boolean = false,
    size: Dp = 44.dp,
    tint: Color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color(0xFF1E293B)),
    testTag: String = "glass_icon_btn"
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = if (isSelected) {
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            if (isDark) Color(0x33FFFFFF) else Color(0x80FFFFFF),
                            if (isDark) Color(0x0DFFFFFF) else Color(0x33FFFFFF)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color(0x4DFFFFFF),
                        Color(0x10FFFFFF)
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    icon: ImageVector? = null,
    isDark: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String = "glass_pill_btn"
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 38.dp)
            .clip(CircleShape)
            .background(
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.18f)
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
                        if (isSelected) accentColor else (if (isDark) Color(0x40FFFFFF) else Color(0x80FFFFFF)),
                        if (isSelected) accentColor.copy(alpha = 0.3f) else Color(0x10FFFFFF)
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else (if (isDark) Color.White else Color(0xFF1E293B)),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (isSelected) accentColor else (if (isDark) Color.White else Color(0xFF1E293B)),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun GlassAdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    testTag: String = "adj_slider"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .width(82.dp)
                .clickable { onReset() }
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )

        Text(
            text = displayValue,
            color = if (value != 0f) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .width(48.dp)
                .clickable { onReset() },
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
