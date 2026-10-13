package com.example.ui.screens.luts

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.lut.CubeLutParser
import com.example.data.model.LutPreset
import com.example.data.model.PresetCatalog
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPillButton
import com.example.ui.screens.editor.EditorViewModel
import com.example.ui.theme.liquidGlass
import java.util.Locale

@Composable
fun LutsScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compactModePreference: Boolean = false
) {
    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp < 380 || compactModePreference
    val recipe by viewModel.recipe.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showCustomiseDialog by remember { mutableStateOf(false) }
    var customPresetName by remember { mutableStateOf("") }
    var lutIntensity by remember { mutableFloatStateOf(recipe.lutIntensity) }

    // Launcher for importing .cube 3D LUT files
    val cubePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val cubeData = CubeLutParser.parse(stream)
                    if (cubeData != null) {
                        viewModel.setCustomCubeLut(cubeData)
                        Toast.makeText(context, "Loaded .cube: ${cubeData.title} (${cubeData.size}³)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid or unsupported .cube file format", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading .cube file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredPresets = remember(selectedCategory, searchQuery) {
        PresetCatalog.bundledPresets.filter { preset ->
            val matchesCategory = selectedCategory == "All" || preset.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    preset.name.contains(searchQuery, ignoreCase = true) ||
                    preset.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(if (isDark) Color(0xFF090D14) else Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    isDark = isDark,
                    size = 40.dp,
                    testTag = "luts_back_btn"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Camera LUTs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Search Presets",
                    onClick = { isSearchActive = !isSearchActive },
                    isDark = isDark,
                    size = 40.dp,
                    testTag = "luts_search_btn"
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlassIconButton(
                    icon = Icons.Default.FileOpen,
                    contentDescription = "Import .cube LUT",
                    onClick = {
                        cubePickerLauncher.launch(arrayOf("*/*"))
                    },
                    isDark = isDark,
                    size = 40.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    testTag = "luts_cube_import_btn"
                )
            }
        }

        // Search Input
        if (isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 110+ camera presets...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .testTag("luts_search_input"),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetCatalog.categories.forEach { cat ->
                GlassPillButton(
                    text = cat,
                    isSelected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    isDark = isDark,
                    testTag = "lut_cat_${cat.lowercase()}"
                )
            }
        }

        // Featured Card: "Fuji Film" (When "All" or "Film" selected)
        if (selectedCategory == "All" || selectedCategory == "Film") {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSmallScreen) 95.dp else 130.dp)
                    .clickable { selectedCategory = "Film" }
                    .testTag("featured_fuji_card"),
                shape = RoundedCornerShape(20.dp),
                isDark = isDark
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.fuji_pagoda_1791818064774),
                        contentDescription = "Fuji Film Featured",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x33000000), Color(0xDD090D14))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Fuji Film",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .liquidGlass(shape = CircleShape, isDark = true, glassIntensity = 0.9f)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "12 LUTs",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Text(
                            text = "Classic tones, timeless vibes.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Grid of Presets
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filteredPresets) { preset ->
                val isSelected = recipe.lutId == preset.id
                LutPresetCard(
                    preset = preset,
                    isSelected = isSelected,
                    onClick = {
                        viewModel.applyLut(preset.id, lutIntensity)
                    },
                    isDark = isDark
                )
            }
        }

        // Bottom Intensity & Customise Bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(22.dp),
            isDark = isDark,
            glassIntensity = 0.92f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Preset Intensity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intensity",
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(60.dp)
                    )
                    Slider(
                        value = lutIntensity,
                        onValueChange = {
                            lutIntensity = it
                            viewModel.applyLut(recipe.lutId, it)
                        },
                        valueRange = 0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(lutIntensity * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(40.dp)
                    )
                }

                // Customise / Save Preset Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomiseDialog = true }
                        .padding(vertical = 4.dp)
                        .testTag("luts_customise_btn"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Recipe as Custom Preset",
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Save Custom Preset Dialog
    if (showCustomiseDialog) {
        AlertDialog(
            onDismissRequest = { showCustomiseDialog = false },
            title = { Text("Save Custom Look") },
            text = {
                Column {
                    Text("Save your current adjustments as a reusable preset:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customPresetName,
                        onValueChange = { customPresetName = it },
                        placeholder = { Text("e.g. My Cinematic Gold") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customPresetName.isNotBlank()) {
                            Toast.makeText(context, "Preset \"$customPresetName\" saved!", Toast.LENGTH_SHORT).show()
                            showCustomiseDialog = false
                            customPresetName = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomiseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LutPresetCard(
    preset: LutPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("lut_item_${preset.id}"),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preset.name,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color(0xFF0F172A)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = preset.description,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 10.sp,
                    maxLines = 2,
                    lineHeight = 13.sp
                )

                Text(
                    text = preset.category.uppercase(),
                    color = Color(0xFFA78BFA),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
