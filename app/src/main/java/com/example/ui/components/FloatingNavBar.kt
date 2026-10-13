package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.liquidGlass

enum class NavDestination(val label: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "nav_home"),
    EDIT("Edit", Icons.Default.Tune, "nav_edit"),
    LUTS("LUTs", Icons.Default.AutoAwesome, "nav_luts"),
    TOOLS("Tools", Icons.Default.Crop, "nav_tools"),
    EXPORT("Export", Icons.Default.FileUpload, "nav_export")
}

@Composable
fun FloatingNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    glassIntensity: Float = 0.85f,
    isCompact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = if (isCompact) 12.dp else 20.dp,
                vertical = if (isCompact) 4.dp else 8.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(32.dp),
                    isDark = isDark,
                    glassIntensity = glassIntensity,
                    borderWidth = 1.dp,
                    elevation = if (isCompact) 8.dp else 12.dp
                )
                .padding(
                    horizontal = if (isCompact) 6.dp else 8.dp,
                    vertical = if (isCompact) 3.dp else 6.dp
                ),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavDestination.values().forEach { destination ->
                val isSelected = destination == currentDestination
                val accentColor = MaterialTheme.colorScheme.primary

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(destination) }
                        )
                        .padding(
                            horizontal = if (isCompact) 6.dp else 10.dp,
                            vertical = if (isCompact) 2.dp else 4.dp
                        )
                        .testTag(destination.tag),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 28.dp else 34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) accentColor.copy(alpha = if (isDark) 0.25f else 0.20f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = if (isSelected) accentColor else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                            modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isCompact) 1.dp else 2.dp))

                    Text(
                        text = destination.label,
                        fontSize = if (isCompact) 9.5.sp else 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) accentColor else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                    )
                }
            }
        }
    }
}
