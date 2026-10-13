package com.example.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppTheme
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePreferences
import com.example.data.repository.SettingsRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPillButton
import com.example.ui.components.PermissionHelper
import android.widget.Toast
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferences: ThemePreferences,
    settingsRepository: SettingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compactModePreference: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permissionsGranted by remember { mutableStateOf(PermissionHelper.hasAllPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    val themes = listOf(
        AppTheme.OBSIDIAN to listOf(Color(0xFF00F0FF), Color(0xFF38BDF8)),
        AppTheme.CLOUD to listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
        AppTheme.MINT to listOf(Color(0xFF10B981), Color(0xFF34D399)),
        AppTheme.AURORA to listOf(Color(0xFFA78BFA), Color(0xFFF472B6)),
        AppTheme.ROSE to listOf(Color(0xFFFB7185), Color(0xFFFDA4AF)),
        AppTheme.CUSTOM to listOf(Color(0xFFFFB800), Color(0xFFFF8A00))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(if (isDark) Color(0xFF090D14) else Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                isDark = isDark,
                size = 40.dp,
                testTag = "settings_back_btn"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Preferences & System",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Appearance Mode (Light, Dark, System)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Appearance Mode",
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassPillButton(
                        text = "System",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = preferences.mode == ThemeMode.SYSTEM,
                        onClick = { scope.launch { settingsRepository.setMode(ThemeMode.SYSTEM) } },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    GlassPillButton(
                        text = "Dark",
                        icon = Icons.Default.DarkMode,
                        isSelected = preferences.mode == ThemeMode.DARK,
                        onClick = { scope.launch { settingsRepository.setMode(ThemeMode.DARK) } },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    GlassPillButton(
                        text = "Light",
                        icon = Icons.Default.LightMode,
                        isSelected = preferences.mode == ThemeMode.LIGHT,
                        onClick = { scope.launch { settingsRepository.setMode(ThemeMode.LIGHT) } },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Small Display Phone Compact Mode Toggle
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (preferences.compactMode || compactModePreference)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else Color(0x1F00F0FF)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Compact Display Mode",
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Visual Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (preferences.compactMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else if (compactModePreference) Color(0x1F10B981)
                                        else (if (isDark) Color(0x1FFFFFFF) else Color(0x1F000000))
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (preferences.compactMode) "ACTIVE ~83%"
                                    else if (compactModePreference) "AUTO-SCALED"
                                    else "STANDARD",
                                    color = if (preferences.compactMode) MaterialTheme.colorScheme.primary
                                    else if (compactModePreference) Color(0xFF10B981)
                                    else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Dynamically scales the entire user interface so all controls comfortably fit small display phones",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = preferences.compactMode,
                    onCheckedChange = { isChecked ->
                        scope.launch {
                            settingsRepository.setCompactMode(isChecked)
                            Toast.makeText(
                                context,
                                if (isChecked) "Compact Mode enabled: entire UI resized" else "Standard display mode restored",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Automatic Photo Saving Toggle
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1F10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto-Save to Phone Gallery",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically saves high-res photo into device gallery after editing",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = preferences.autoSaveToGallery,
                    onCheckedChange = {
                        scope.launch { settingsRepository.setAutoSaveToGallery(it) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Permissions Status & Manager Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (permissionsGranted) Color(0x1F10B981) else Color(0x1FFFB800)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (permissionsGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (permissionsGranted) Color(0xFF10B981) else Color(0xFFFFB800),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gallery & Notifications Access",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (permissionsGranted) "All permissions granted" else "Tap to grant gallery & notification access",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }

                if (!permissionsGranted) {
                    GlassPillButton(
                        text = "Grant",
                        isSelected = true,
                        onClick = {
                            permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                        },
                        isDark = isDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Liquid Glass Theme Selector
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Liquid Glass Themes",
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select from 6 premium translucent optical themes:",
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                themes.forEach { (theme, colors) ->
                    val isSelected = preferences.theme == theme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                scope.launch { settingsRepository.setTheme(theme) }
                            }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Brush.sweepGradient(colors))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color(0x33FFFFFF),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = theme.title,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color(0xFF0F172A)),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = theme.description,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Glass Intensity Slider
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Glass Translucency & Blur",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(preferences.glassIntensity * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = preferences.glassIntensity,
                    onValueChange = {
                        scope.launch { settingsRepository.setGlassIntensity(it) }
                    },
                    valueRange = 0.4f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Haptic Feedback Toggle
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1F00F0FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Haptic Tactile Feedback",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Subtle vibration when adjusting sliders",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = preferences.hapticEnabled,
                    onCheckedChange = {
                        scope.launch { settingsRepository.setHapticEnabled(it) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // App Info & Attribution Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OGOO — Professional Photo Editor v1.0",
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Made with ❤️ by OGOO | ogoo.edits.apps",
                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
