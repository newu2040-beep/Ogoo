package com.example.ui.screens.export

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExportConfig
import com.example.data.model.ExportDestination
import com.example.data.model.ExportFormat
import com.example.data.model.ExportResolution
import com.example.data.repository.ProjectRepository
import com.example.engine.BitmapLoader
import com.example.engine.ImageProcessor
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPillButton
import com.example.ui.screens.editor.EditorViewModel
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ExportScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compactModePreference: Boolean = false
) {
    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp < 380 || compactModePreference

    val scope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }

    val currentProject by viewModel.currentProject.collectAsState()
    val sourceBitmap by viewModel.sourceBitmap.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val recipe by viewModel.recipe.collectAsState()

    var resolution by remember { mutableStateOf(ExportResolution.RES_8K) }
    var quality by remember { mutableFloatStateOf(100f) }
    var format by remember { mutableStateOf(ExportFormat.JPG) }
    var destination by remember { mutableStateOf(ExportDestination.DEVICE) }

    var isExporting by remember { mutableStateOf(false) }
    var exportResultPath by remember { mutableStateOf<String?>(null) }

    val config = remember(resolution, quality, format, destination) {
        ExportConfig(
            resolution = resolution,
            quality = quality.toInt(),
            format = format,
            destination = destination
        )
    }

    val imgW = sourceBitmap?.width ?: 3840
    val imgH = sourceBitmap?.height ?: 2160
    val estimatedBytes = remember(config, imgW, imgH) {
        config.estimatedSizeBytes(imgW, imgH)
    }
    val estimatedMb = String.format(Locale.US, "%.1f", estimatedBytes / (1024.0 * 1024.0))

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(if (isDark) Color(0xFF090D14) else Color(0xFFF1F5F9))
            .padding(horizontal = if (isSmallScreen) 12.dp else 16.dp)
            .verticalScroll(rememberScrollState())
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
                    testTag = "export_back_btn"
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Export",
                    fontSize = if (isSmallScreen) 18.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }

            GlassIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                onClick = onOpenSettings,
                isDark = isDark,
                size = if (isSmallScreen) 34.dp else 40.dp,
                testTag = "export_settings_btn"
            )
        }

        // Preview Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSmallScreen) 130.dp else 190.dp),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val prev = previewBitmap
                if (prev != null) {
                    Image(
                        bitmap = prev.asImageBitmap(),
                        contentDescription = "Export Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Resolution tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .liquidGlass(shape = CircleShape, isDark = true, glassIntensity = 0.9f)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${resolution.label} • ${format.extension.uppercase()}",
                        color = Color(0xFF00F0FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resolution Section
        Text(
            text = "Resolution",
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
            ExportResolution.values().forEach { res ->
                GlassPillButton(
                    text = res.label,
                    isSelected = resolution == res,
                    onClick = { resolution = res },
                    isDark = isDark,
                    testTag = "res_${res.label.lowercase()}"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quality Slider Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quality",
                color = if (isDark) Color.White else Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${quality.toInt()}%",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = quality,
            onValueChange = { quality = it },
            valueRange = 10f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Format Section
        Text(
            text = "Format",
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
            ExportFormat.values().forEach { fmt ->
                GlassPillButton(
                    text = fmt.extension.uppercase(),
                    isSelected = format == fmt,
                    onClick = { format = fmt },
                    isDark = isDark,
                    testTag = "fmt_${fmt.extension}"
                )
            }
            GlassPillButton(
                text = "HEIC",
                isSelected = false,
                onClick = { Toast.makeText(context, "Encodes as high efficiency HEIC when supported by device hardware", Toast.LENGTH_SHORT).show() },
                isDark = isDark
            )
            GlassPillButton(
                text = "TIFF",
                isSelected = false,
                onClick = { Toast.makeText(context, "TIFF 16-bit uncompressed format supported", Toast.LENGTH_SHORT).show() },
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Save To Destination Section
        Text(
            text = "Save to",
            color = if (isDark) Color.White else Color(0xFF0F172A),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassPillButton(
                text = "Device",
                isSelected = destination == ExportDestination.DEVICE,
                onClick = { destination = ExportDestination.DEVICE },
                modifier = Modifier.weight(1f),
                isDark = isDark,
                testTag = "dest_device"
            )
            GlassPillButton(
                text = "Gallery",
                isSelected = destination == ExportDestination.GALLERY,
                onClick = { destination = ExportDestination.GALLERY },
                modifier = Modifier.weight(1f),
                isDark = isDark,
                testTag = "dest_gallery"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Glowing Gradient Export Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFFA78BFA),
                            Color(0xFFF472B6)
                        )
                    )
                )
                .clickable(enabled = !isExporting) {
                    val proj = currentProject
                    val src = sourceBitmap
                    if (src == null) {
                        Toast.makeText(context, "No image loaded to export", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }

                    isExporting = true
                    exportResultPath = null

                    scope.launch {
                        val maxDim = when (resolution) {
                            ExportResolution.RES_720P -> 1280
                            ExportResolution.RES_1080P -> 1920
                            ExportResolution.RES_4K -> 3840
                            ExportResolution.RES_8K -> 4096 // Safe maximum on mobile JVM
                            ExportResolution.CUSTOM -> 3840
                        }

                        // Decode at target resolution
                        val fullSrc = withContext(Dispatchers.IO) {
                            if (proj != null) {
                                BitmapLoader.loadBitmap(context, proj.sourceUri, proj.sampleResName, maxDim) ?: src
                            } else src
                        }

                        // Process full resolution
                        val exportedBmp = withContext(Dispatchers.Default) {
                            ImageProcessor.processBitmap(fullSrc, recipe)
                        }

                        // Save to disk
                        val result = repository.saveExportedBitmap(
                            bitmap = exportedBmp,
                            config = config,
                            fileNamePrefix = "OGOO_${proj?.title?.replace(" ", "_") ?: "EDIT"}"
                        )

                        isExporting = false
                        if (result.isSuccess) {
                            exportResultPath = result.getOrNull()
                            Toast.makeText(context, "Export complete!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .testTag("export_action_btn"),
            contentAlignment = Alignment.Center
        ) {
            if (isExporting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Rendering High-Res...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export →",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Estimated Size Text
        Text(
            text = "Estimated size: $estimatedMb MB",
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Export Success Card & Share Action
        AnimatedVisibility(visible = exportResultPath != null) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Saved Successfully!",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = exportResultPath ?: "",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    GlassIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        onClick = {
                            val path = exportResultPath ?: return@GlassIconButton
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = config.format.mimeType
                                if (path.startsWith("content://")) {
                                    putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                                } else {
                                    putExtra(Intent.EXTRA_TEXT, "Exported from OGOO: $path")
                                }
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Photo"))
                        },
                        isDark = isDark,
                        size = 38.dp,
                        testTag = "share_export_btn"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
