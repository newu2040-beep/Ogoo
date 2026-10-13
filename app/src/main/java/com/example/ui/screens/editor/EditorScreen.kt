package com.example.ui.screens.editor

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.ColorGradingWheel
import com.example.ui.components.CurveCanvas
import com.example.ui.components.GlassAdjustmentSlider
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPillButton
import com.example.ui.components.HistogramView
import com.example.ui.theme.liquidGlass
import java.util.Locale

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onOpenLuts: () -> Unit,
    onToggleCompactMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compactModePreference: Boolean = false
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp < 380 || compactModePreference

    val sourceBitmap by viewModel.sourceBitmap.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val recipe by viewModel.recipe.collectAsState()
    val histogram by viewModel.histogram.collectAsState()
    val isSplitMode by viewModel.isSplitMode.collectAsState()
    val isRendering by viewModel.isRendering.collectAsState()
    val autoSaveStatus by viewModel.autoSaveStatus.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    var activeCategory by remember { mutableStateOf("Light") }
    var selectedHslChannel by remember { mutableIntStateOf(0) }
    var selectedCurveChannel by remember { mutableStateOf("Master") }

    val hslChannelNames = listOf("Red", "Orange", "Yellow", "Green", "Aqua", "Blue", "Purple", "Magenta")
    val hslChannelColors = listOf(
        Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
        Color(0xFF34C759), Color(0xFF00C7BE), Color(0xFF007AFF),
        Color(0xFF5856D6), Color(0xFFAF52DE)
    )

    // Photo picker launcher for direct gallery photo import
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadFromUri(uri.toString())
        }
    }

    // Camera launcher for live photo capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            viewModel.loadFromBitmap(bmp)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF090D14) else Color(0xFFF1F5F9))
            .statusBarsPadding()
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 10.dp else 16.dp, vertical = if (isSmallScreen) 4.dp else 8.dp),
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
                    testTag = "editor_back_btn"
                )

                Spacer(modifier = Modifier.width(6.dp))

                // RAW Format Badge
                Box(
                    modifier = Modifier
                        .liquidGlass(shape = CircleShape, isDark = isDark, glassIntensity = 0.8f)
                        .padding(horizontal = if (isSmallScreen) 8.dp else 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RAW ▾",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = if (isSmallScreen) 10.sp else 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Automatic save status pill
                if (autoSaveStatus != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .liquidGlass(shape = CircleShape, isDark = isDark, glassIntensity = 0.85f)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = autoSaveStatus ?: "",
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Compact display mode quick toggle
                if (onToggleCompactMode != null) {
                    GlassIconButton(
                        icon = Icons.Default.AspectRatio,
                        contentDescription = if (isSmallScreen) "Compact Mode (Active)" else "Compact Mode (Standard)",
                        onClick = onToggleCompactMode,
                        isDark = isDark,
                        size = if (isSmallScreen) 34.dp else 38.dp,
                        tint = if (isSmallScreen) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)),
                        testTag = "editor_compact_toggle_btn"
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Import photo button
                GlassIconButton(
                    icon = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Import Photo",
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 38.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    testTag = "editor_import_photo_btn"
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Quick 1-tap Save to Phone action
                GlassIconButton(
                    icon = Icons.Default.SaveAlt,
                    contentDescription = "Save to Phone Now",
                    onClick = {
                        viewModel.saveToPhoneNow { success ->
                            if (success) {
                                Toast.makeText(context, "Saved photo directly to phone gallery!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 38.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    testTag = "editor_quick_save_btn"
                )

                Spacer(modifier = Modifier.width(4.dp))

                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    onClick = { viewModel.undo() },
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 38.dp,
                    tint = if (canUndo) (if (isDark) Color.White else Color(0xFF0F172A)) else Color(0x33888888),
                    testTag = "editor_undo_btn"
                )

                Spacer(modifier = Modifier.width(4.dp))

                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    onClick = { viewModel.redo() },
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 38.dp,
                    tint = if (canRedo) (if (isDark) Color.White else Color(0xFF0F172A)) else Color(0x33888888),
                    testTag = "editor_redo_btn"
                )

                Spacer(modifier = Modifier.width(4.dp))

                GlassIconButton(
                    icon = Icons.Default.Compare,
                    contentDescription = "Before / After Split",
                    onClick = { viewModel.toggleSplitMode() },
                    isDark = isDark,
                    isSelected = isSplitMode,
                    size = if (isSmallScreen) 34.dp else 38.dp,
                    testTag = "editor_compare_btn"
                )
            }
        }

        // Image Canvas with Floating Histogram
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = if (isSmallScreen) 8.dp else 12.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            val src = sourceBitmap
            val prev = previewBitmap

            if (src != null && prev != null) {
                if (isSplitMode) {
                    BeforeAfterSlider(
                        beforeBitmap = src,
                        afterBitmap = prev,
                        isSplitMode = true
                    )
                } else {
                    Image(
                        bitmap = prev.asImageBitmap(),
                        contentDescription = "Edited Image Canvas",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            } else {
                GlassCard(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    isDark = isDark,
                    glassIntensity = 0.9f
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSmallScreen) 44.dp else 52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(if (isSmallScreen) 22.dp else 26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "No Photo Loaded",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = if (isSmallScreen) 15.sp else 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Choose any photo from your phone or capture a live shot to edit in real time.",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = if (isSmallScreen) 11.sp else 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassPillButton(
                                text = "From Gallery",
                                icon = Icons.Default.AddPhotoAlternate,
                                isSelected = true,
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                isDark = isDark,
                                testTag = "editor_empty_gallery_btn"
                            )

                            GlassPillButton(
                                text = "Take Camera",
                                icon = Icons.Default.PhotoCamera,
                                isSelected = false,
                                onClick = { cameraLauncher.launch(null) },
                                isDark = isDark,
                                testTag = "editor_empty_camera_btn"
                            )
                        }
                    }
                }
            }

            // Floating Real-time Histogram in bottom right
            HistogramView(
                data = histogram,
                isDark = isDark,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            )

            // Rendering indicator badge
            if (isRendering) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .liquidGlass(shape = CircleShape, isDark = isDark, glassIntensity = 0.9f)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Rendering...",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Floating Adjustment Panel
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 8.dp else 12.dp, vertical = 2.dp),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark,
            glassIntensity = 0.92f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isSmallScreen) 8.dp else 12.dp)
            ) {
                // Category Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Light", "Color", "Effects", "Detail", "HSL", "Curves", "Wheels").forEach { cat ->
                        GlassPillButton(
                            text = cat,
                            isSelected = activeCategory == cat,
                            onClick = { activeCategory = cat },
                            isDark = isDark,
                            testTag = "cat_${cat.lowercase()}"
                        )
                    }

                    // Reset category button
                    GlassIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Reset category",
                        onClick = { viewModel.resetCategory(activeCategory) },
                        isDark = isDark,
                        size = if (isSmallScreen) 30.dp else 36.dp,
                        testTag = "reset_cat_btn"
                    )
                }

                Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 10.dp))

                // Adjustment Sliders based on activeCategory
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSmallScreen) 105.dp else 135.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (activeCategory) {
                        "Light" -> {
                            GlassAdjustmentSlider(
                                label = "Exposure",
                                value = recipe.exposure,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(exposure = it)) },
                                valueRange = -2.0f..2.0f,
                                displayValue = String.format(Locale.US, "%+.2f", recipe.exposure),
                                onReset = { viewModel.updateRecipe(recipe.copy(exposure = 0f), true) },
                                isDark = isDark,
                                testTag = "slider_exposure"
                            )
                            GlassAdjustmentSlider(
                                label = "Contrast",
                                value = recipe.contrast,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(contrast = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.contrast),
                                onReset = { viewModel.updateRecipe(recipe.copy(contrast = 0f), true) },
                                isDark = isDark,
                                testTag = "slider_contrast"
                            )
                            GlassAdjustmentSlider(
                                label = "Highlights",
                                value = recipe.highlights,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(highlights = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.highlights),
                                onReset = { viewModel.updateRecipe(recipe.copy(highlights = 0f), true) },
                                isDark = isDark,
                                testTag = "slider_highlights"
                            )
                            GlassAdjustmentSlider(
                                label = "Shadows",
                                value = recipe.shadows,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(shadows = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.shadows),
                                onReset = { viewModel.updateRecipe(recipe.copy(shadows = 0f), true) },
                                isDark = isDark,
                                testTag = "slider_shadows"
                            )
                            GlassAdjustmentSlider(
                                label = "Whites",
                                value = recipe.whites,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(whites = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.whites),
                                onReset = { viewModel.updateRecipe(recipe.copy(whites = 0f), true) },
                                isDark = isDark,
                                testTag = "slider_whites"
                            )
                            GlassAdjustmentSlider(
                                label = "Blacks",
                                value = recipe.blacks,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(blacks = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.blacks),
                                onReset = { viewModel.updateRecipe(recipe.copy(blacks = 0f), true) },
                                isDark = isDark,
                                testTag = "slider_blacks"
                            )
                        }
                        "Color" -> {
                            GlassAdjustmentSlider(
                                label = "Temp",
                                value = recipe.temperature,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(temperature = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.temperature),
                                onReset = { viewModel.updateRecipe(recipe.copy(temperature = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Tint",
                                value = recipe.tint,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(tint = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.tint),
                                onReset = { viewModel.updateRecipe(recipe.copy(tint = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Vibrance",
                                value = recipe.vibrance,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(vibrance = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.vibrance),
                                onReset = { viewModel.updateRecipe(recipe.copy(vibrance = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Saturation",
                                value = recipe.saturation,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(saturation = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.saturation),
                                onReset = { viewModel.updateRecipe(recipe.copy(saturation = 0f), true) },
                                isDark = isDark
                            )
                        }
                        "Effects" -> {
                            GlassAdjustmentSlider(
                                label = "Texture",
                                value = recipe.texture,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(texture = it)) },
                                valueRange = 0f..100f,
                                displayValue = String.format(Locale.US, "%.0f", recipe.texture),
                                onReset = { viewModel.updateRecipe(recipe.copy(texture = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Clarity",
                                value = recipe.clarity,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(clarity = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.clarity),
                                onReset = { viewModel.updateRecipe(recipe.copy(clarity = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Dehaze",
                                value = recipe.dehaze,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(dehaze = it)) },
                                valueRange = 0f..100f,
                                displayValue = String.format(Locale.US, "%.0f", recipe.dehaze),
                                onReset = { viewModel.updateRecipe(recipe.copy(dehaze = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Vignette",
                                value = recipe.vignetteAmount,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(vignetteAmount = it)) },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", recipe.vignetteAmount),
                                onReset = { viewModel.updateRecipe(recipe.copy(vignetteAmount = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Film Grain",
                                value = recipe.filmGrainAmount,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(filmGrainAmount = it)) },
                                valueRange = 0f..100f,
                                displayValue = String.format(Locale.US, "%.0f", recipe.filmGrainAmount),
                                onReset = { viewModel.updateRecipe(recipe.copy(filmGrainAmount = 0f), true) },
                                isDark = isDark
                            )
                        }
                        "Detail" -> {
                            GlassAdjustmentSlider(
                                label = "Sharpening",
                                value = recipe.sharpening,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(sharpening = it)) },
                                valueRange = 0f..100f,
                                displayValue = String.format(Locale.US, "%.0f", recipe.sharpening),
                                onReset = { viewModel.updateRecipe(recipe.copy(sharpening = 0f), true) },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Noise Red.",
                                value = recipe.noiseReduction,
                                onValueChange = { viewModel.updateRecipe(recipe.copy(noiseReduction = it)) },
                                valueRange = 0f..100f,
                                displayValue = String.format(Locale.US, "%.0f", recipe.noiseReduction),
                                onReset = { viewModel.updateRecipe(recipe.copy(noiseReduction = 0f), true) },
                                isDark = isDark
                            )
                        }
                        "HSL" -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                hslChannelNames.forEachIndexed { index, name ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(hslChannelColors[index])
                                            .clickable { selectedHslChannel = index }
                                            .padding(2.dp)
                                    ) {
                                        if (selectedHslChannel == index) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.5f))
                                            )
                                        }
                                    }
                                }
                            }

                            val curHues = recipe.hslHues.toMutableList()
                            val curSats = recipe.hslSaturations.toMutableList()
                            val curLums = recipe.hslLuminances.toMutableList()

                            GlassAdjustmentSlider(
                                label = "Hue",
                                value = curHues[selectedHslChannel],
                                onValueChange = {
                                    curHues[selectedHslChannel] = it
                                    viewModel.updateRecipe(recipe.copy(hslHues = curHues))
                                },
                                valueRange = -180f..180f,
                                displayValue = String.format(Locale.US, "%+.0f°", curHues[selectedHslChannel]),
                                onReset = {
                                    curHues[selectedHslChannel] = 0f
                                    viewModel.updateRecipe(recipe.copy(hslHues = curHues), true)
                                },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Saturation",
                                value = curSats[selectedHslChannel],
                                onValueChange = {
                                    curSats[selectedHslChannel] = it
                                    viewModel.updateRecipe(recipe.copy(hslSaturations = curSats))
                                },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", curSats[selectedHslChannel]),
                                onReset = {
                                    curSats[selectedHslChannel] = 0f
                                    viewModel.updateRecipe(recipe.copy(hslSaturations = curSats), true)
                                },
                                isDark = isDark
                            )
                            GlassAdjustmentSlider(
                                label = "Luminance",
                                value = curLums[selectedHslChannel],
                                onValueChange = {
                                    curLums[selectedHslChannel] = it
                                    viewModel.updateRecipe(recipe.copy(hslLuminances = curLums))
                                },
                                valueRange = -100f..100f,
                                displayValue = String.format(Locale.US, "%+.0f", curLums[selectedHslChannel]),
                                onReset = {
                                    curLums[selectedHslChannel] = 0f
                                    viewModel.updateRecipe(recipe.copy(hslLuminances = curLums), true)
                                },
                                isDark = isDark
                            )
                        }
                        "Curves" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("Master", "Red", "Green", "Blue").forEach { channel ->
                                    GlassPillButton(
                                        text = channel,
                                        isSelected = selectedCurveChannel == channel,
                                        onClick = { selectedCurveChannel = channel },
                                        isDark = isDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val curvePts = when (selectedCurveChannel) {
                                "Red" -> recipe.curvePointsRed
                                "Green" -> recipe.curvePointsGreen
                                "Blue" -> recipe.curvePointsBlue
                                else -> recipe.curvePointsMaster
                            }
                            val curveColor = when (selectedCurveChannel) {
                                "Red" -> Color(0xFFFF453A)
                                "Green" -> Color(0xFF30D158)
                                "Blue" -> Color(0xFF0A84FF)
                                else -> if (isDark) Color.White else Color(0xFF0F172A)
                            }

                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CurveCanvas(
                                    points = curvePts,
                                    onPointsChanged = { newPts ->
                                        val updated = when (selectedCurveChannel) {
                                            "Red" -> recipe.copy(curvePointsRed = newPts)
                                            "Green" -> recipe.copy(curvePointsGreen = newPts)
                                            "Blue" -> recipe.copy(curvePointsBlue = newPts)
                                            else -> recipe.copy(curvePointsMaster = newPts)
                                        }
                                        viewModel.updateRecipe(updated)
                                    },
                                    curveColor = curveColor,
                                    isDark = isDark
                                )
                            }
                        }
                        "Wheels" -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    }
                }
            }
        }

        // Bottom Tool Capsules: Auto, Light, Color, Effects, Detail, Preset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 10.dp else 16.dp, vertical = if (isSmallScreen) 4.dp else 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolCapsule(
                icon = Icons.Default.AutoFixHigh,
                label = "Auto",
                isActive = false,
                onClick = { viewModel.applyAutoTone() },
                isDark = isDark,
                isCompact = isSmallScreen,
                testTag = "tool_auto"
            )
            ToolCapsule(
                icon = Icons.Default.WbSunny,
                label = "Light",
                isActive = activeCategory == "Light",
                onClick = { activeCategory = "Light" },
                isDark = isDark,
                isCompact = isSmallScreen,
                testTag = "tool_light"
            )
            ToolCapsule(
                icon = Icons.Default.Palette,
                label = "Color",
                isActive = activeCategory == "Color",
                onClick = { activeCategory = "Color" },
                isDark = isDark,
                isCompact = isSmallScreen,
                testTag = "tool_color"
            )
            ToolCapsule(
                icon = Icons.Default.FilterDrama,
                label = "Effects",
                isActive = activeCategory == "Effects",
                onClick = { activeCategory = "Effects" },
                isDark = isDark,
                isCompact = isSmallScreen,
                testTag = "tool_effects"
            )
            ToolCapsule(
                icon = Icons.Default.Details,
                label = "Detail",
                isActive = activeCategory == "Detail",
                onClick = { activeCategory = "Detail" },
                isDark = isDark,
                isCompact = isSmallScreen,
                testTag = "tool_detail"
            )
            ToolCapsule(
                icon = Icons.Default.AutoAwesome,
                label = "Preset",
                isActive = false,
                onClick = onOpenLuts,
                isDark = isDark,
                isCompact = isSmallScreen,
                testTag = "tool_preset"
            )
        }
    }
}

@Composable
private fun ToolCapsule(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    isCompact: Boolean,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 32.dp else 38.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)),
                modifier = Modifier.size(if (isCompact) 17.dp else 20.dp)
            )
        }
        Text(
            text = label,
            fontSize = if (isCompact) 9.5.sp else 11.sp,
            color = if (isActive) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
