package com.example.ui.screens.tools

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ColorGradingWheel
import com.example.ui.components.CurveCanvas
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPillButton
import com.example.ui.screens.editor.EditorViewModel
import com.example.ui.theme.liquidGlass

@Composable
fun ToolsScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compactModePreference: Boolean = false
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp < 380 || compactModePreference

    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val recipe by viewModel.recipe.collectAsState()

    var activeTool by remember { mutableStateOf("Crop") }
    val cropAspects = listOf("Original", "1:1", "4:5", "16:9", "9:16", "3:2")

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(if (isDark) Color(0xFF090D14) else Color(0xFFF1F5F9))
            .padding(horizontal = if (isSmallScreen) 10.dp else 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isSmallScreen) 6.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 40.dp,
                    testTag = "tools_back_btn"
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Professional Tools",
                    fontSize = if (isSmallScreen) 17.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }
        }

        // Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSmallScreen) 160.dp else 240.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isDark) Color(0xFF121724) else Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            val bmp = previewBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "No photo loaded for tools preview",
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 12.sp
                )
            }

            // Aspect Ratio Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .liquidGlass(shape = CircleShape, isDark = true, glassIntensity = 0.9f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Crop: ${recipe.cropAspect} | ${recipe.rotationDegrees.toInt()}°",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tool Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Crop", "Curves", "Color Grading", "Masks", "Retouch").forEach { tool ->
                GlassPillButton(
                    text = tool,
                    isSelected = activeTool == tool,
                    onClick = { activeTool = tool },
                    isDark = isDark,
                    testTag = "tool_tab_${tool.lowercase().replace(" ", "_")}"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tool Content
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(22.dp),
            isDark = isDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (activeTool) {
                    "Crop" -> {
                        Text(
                            text = "Aspect Ratio",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cropAspects.forEach { aspect ->
                                GlassPillButton(
                                    text = aspect,
                                    isSelected = recipe.cropAspect == aspect,
                                    onClick = {
                                        viewModel.updateRecipe(recipe.copy(cropAspect = aspect), true)
                                    },
                                    isDark = isDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Transform & Rotate",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassPillButton(
                                text = "Rotate 90°",
                                icon = Icons.AutoMirrored.Filled.RotateRight,
                                onClick = {
                                    val nextRot = (recipe.rotationDegrees + 90f) % 360f
                                    viewModel.updateRecipe(recipe.copy(rotationDegrees = nextRot), true)
                                },
                                isDark = isDark
                            )
                            GlassPillButton(
                                text = "Flip H",
                                icon = Icons.Default.Flip,
                                isSelected = recipe.flipHorizontal,
                                onClick = {
                                    viewModel.updateRecipe(recipe.copy(flipHorizontal = !recipe.flipHorizontal), true)
                                },
                                isDark = isDark
                            )
                            GlassPillButton(
                                text = "Flip V",
                                icon = Icons.Default.Flip,
                                isSelected = recipe.flipVertical,
                                onClick = {
                                    viewModel.updateRecipe(recipe.copy(flipVertical = !recipe.flipVertical), true)
                                },
                                isDark = isDark
                            )
                        }
                    }
                    "Curves" -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CurveCanvas(
                                points = recipe.curvePointsMaster,
                                onPointsChanged = { newPts ->
                                    viewModel.updateRecipe(recipe.copy(curvePointsMaster = newPts))
                                },
                                curveColor = Color.White,
                                isDark = isDark
                            )
                        }
                    }
                    "Color Grading" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ColorGradingWheel(
                                title = "Shadows",
                                hue = recipe.shadowsHue,
                                saturation = recipe.shadowsSat,
                                onColorChanged = { h, s ->
                                    viewModel.updateRecipe(recipe.copy(shadowsHue = h, shadowsSat = s))
                                },
                                isDark = isDark
                            )
                            ColorGradingWheel(
                                title = "Midtones",
                                hue = recipe.midtonesHue,
                                saturation = recipe.midtonesSat,
                                onColorChanged = { h, s ->
                                    viewModel.updateRecipe(recipe.copy(midtonesHue = h, midtonesSat = s))
                                },
                                isDark = isDark
                            )
                            ColorGradingWheel(
                                title = "Highlights",
                                hue = recipe.highlightsHue,
                                saturation = recipe.highlightsSat,
                                onColorChanged = { h, s ->
                                    viewModel.updateRecipe(recipe.copy(highlightsHue = h, highlightsSat = s))
                                },
                                isDark = isDark
                            )
                        }
                    }
                    "Masks" -> {
                        Text(
                            text = "Selective Masks",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add localized exposure, contrast, and color gradients to skies, portraits, or subjects.",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassPillButton(text = "+ Radial Mask", onClick = { }, isDark = isDark)
                            GlassPillButton(text = "+ Linear Sky", onClick = { }, isDark = isDark)
                            GlassPillButton(text = "+ Brush", onClick = { }, isDark = isDark)
                        }
                    }
                    "Retouch" -> {
                        Text(
                            text = "Healing & Blemish Correction",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap on spots or dust to sample surrounding texture and blend seamlessly.",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassPillButton(
                            text = "Heal Blemish Brush",
                            icon = Icons.Default.Healing,
                            isSelected = true,
                            onClick = { },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}
