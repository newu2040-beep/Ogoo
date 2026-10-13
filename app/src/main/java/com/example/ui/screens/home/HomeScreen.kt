package com.example.ui.screens.home

import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.db.ProjectEntity
import com.example.data.repository.ProjectRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPillButton
import com.example.ui.components.PermissionRequestBanner
import com.example.ui.theme.liquidGlass

@Composable
fun HomeScreen(
    onOpenProject: (String) -> Unit,
    onImportUri: (String) -> Unit,
    onImportBitmap: (Bitmap) -> Unit,
    onOpenSettings: () -> Unit,
    onToggleDarkLightMode: () -> Unit,
    onToggleCompactMode: () -> Unit = {},
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compactModePreference: Boolean = false
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp < 380 || compactModePreference

    val repository = remember { ProjectRepository(context) }
    val allProjects by repository.allProjects.collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportUri(uri.toString())
        }
    }

    // Camera capture launcher for real-time photo capturing
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            onImportBitmap(bmp)
        }
    }

    val filteredProjects = remember(allProjects, selectedFilter, searchQuery) {
        allProjects.filter { proj ->
            val matchesSearch = searchQuery.isBlank() || proj.title.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Photos" -> true
                "Favorites" -> proj.isFavorite
                "LUTs" -> proj.recipeJson.contains("lutId")
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF090D14) else Color(0xFFF1F5F9))
            .statusBarsPadding()
            .padding(horizontal = if (isSmallScreen) 12.dp else 16.dp)
    ) {
        // Top Bar: OGOO Logo & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isSmallScreen) 6.dp else 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OGOO",
                fontSize = if (isSmallScreen) 22.sp else 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            if (isDark) Color(0xFFA78BFA) else Color(0xFF0284C7)
                        )
                    )
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Dark / Light mode toggle
                GlassIconButton(
                    icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                    onClick = onToggleDarkLightMode,
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 40.dp,
                    tint = if (isDark) Color(0xFFFFD600) else Color(0xFF0F172A),
                    testTag = "home_theme_toggle_btn"
                )

                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))

                // Quick Compact Mode toggle (Resizes whole UI for small screens)
                GlassIconButton(
                    icon = Icons.Default.AspectRatio,
                    contentDescription = if (compactModePreference) "Compact Mode (Active)" else "Compact Mode (Standard)",
                    onClick = onToggleCompactMode,
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 40.dp,
                    tint = if (compactModePreference) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)),
                    testTag = "home_compact_toggle_btn"
                )

                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))

                GlassIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Search",
                    onClick = { isSearchActive = !isSearchActive },
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 40.dp,
                    testTag = "home_search_btn"
                )

                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))

                // Camera capture button for real-time capture
                GlassIconButton(
                    icon = Icons.Default.PhotoCamera,
                    contentDescription = "Take Photo",
                    onClick = { cameraLauncher.launch(null) },
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 40.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    testTag = "home_camera_btn"
                )

                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))

                // Gallery import button
                GlassIconButton(
                    icon = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Import Photo",
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 40.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    testTag = "home_import_btn"
                )

                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))

                GlassIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    onClick = onOpenSettings,
                    isDark = isDark,
                    size = if (isSmallScreen) 34.dp else 40.dp,
                    testTag = "home_settings_btn"
                )
            }
        }

        // Expandable Search Bar
        if (isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search edits & presets...",
                        fontSize = if (isSmallScreen) 12.sp else 13.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isSmallScreen) 6.dp else 12.dp)
                    .testTag("home_search_input"),
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    focusedContainerColor = if (isDark) Color(0x331E2638) else Color(0x33FFFFFF),
                    unfocusedContainerColor = if (isDark) Color(0x1F1E2638) else Color(0x1FFFFFFF),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color(0x33FFFFFF)
                ),
                singleLine = true
            )
        }

        // Permission Request Banner (allows asking full notification & gallery access)
        PermissionRequestBanner(
            isDark = isDark,
            isCompact = isSmallScreen
        )

        // Filter Tabs: "All", "Photos", "Favorites", "LUTs"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = if (isSmallScreen) 8.dp else 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Photos", "Favorites", "LUTs").forEach { filter ->
                GlassPillButton(
                    text = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    isDark = isDark,
                    testTag = "filter_${filter.lowercase()}"
                )
            }
        }

        // Hero Card: "Create Something Beautiful"
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSmallScreen) 120.dp else 160.dp)
                .clickable {
                    val firstProject = allProjects.firstOrNull()
                    if (firstProject != null) {
                        onOpenProject(firstProject.id)
                    } else {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                }
                .testTag("home_hero_card"),
            isDark = isDark,
            glassIntensity = 0.9f
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            if (isDark) {
                                listOf(Color(0xFF0E1A2E), Color(0xFF1E2638), Color(0xFF2D1B4E))
                            } else {
                                listOf(Color(0xFFE0F2FE), Color(0xFFEDE9FE), Color(0xFFFCE7F3))
                            }
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isSmallScreen) 12.dp else 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Create Something",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = if (isSmallScreen) 16.sp else 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Beautiful",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = if (isSmallScreen) 16.sp else 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (allProjects.isEmpty()) "Tap to import your photos in real-time" else "${allProjects.size} edits saved on device",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = if (isSmallScreen) 11.sp else 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(if (isSmallScreen) 38.dp else 46.dp)
                            .liquidGlass(shape = CircleShape, isDark = isDark, glassIntensity = 0.95f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Edit photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isSmallScreen) 10.dp else 16.dp))

        // Grid of Real User Projects or Empty Library State
        if (filteredProjects.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                isDark = isDark,
                glassIntensity = 0.85f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isSmallScreen) 16.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSmallScreen) 48.dp else 60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSmallScreen) 24.dp else 30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your Photo Library is Ready",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = if (isSmallScreen) 15.sp else 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Import any picture from your device gallery or capture with camera to begin editing in real-time.",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = if (isSmallScreen) 11.sp else 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = if (isSmallScreen) 15.sp else 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassPillButton(
                            text = "Import Gallery",
                            icon = Icons.Default.AddPhotoAlternate,
                            isSelected = true,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            isDark = isDark,
                            testTag = "home_empty_gallery_btn"
                        )

                        GlassPillButton(
                            text = "Take Photo",
                            icon = Icons.Default.PhotoCamera,
                            isSelected = false,
                            onClick = { cameraLauncher.launch(null) },
                            isDark = isDark,
                            testTag = "home_empty_camera_btn"
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp),
                contentPadding = PaddingValues(bottom = if (isSmallScreen) 80.dp else 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProjects) { project ->
                    ProjectCardItem(
                        project = project,
                        onClick = { onOpenProject(project.id) },
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCardItem(
    project: ProjectEntity,
    onClick: () -> Unit,
    isDark: Boolean
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .testTag("project_item_${project.id}"),
        isDark = isDark,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = project.sourceUri,
                contentDescription = project.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x22000000),
                                Color(0xCC0B0F17)
                            )
                        )
                    )
            )

            // Info badge at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = project.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (project.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFB7185),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (project.isFavorite) "Favorite" else "Edited",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
