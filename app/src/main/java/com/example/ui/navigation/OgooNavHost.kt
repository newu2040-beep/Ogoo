package com.example.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePreferences
import com.example.data.repository.ProjectRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.components.FloatingNavBar
import com.example.ui.components.NavDestination
import com.example.ui.screens.editor.EditorScreen
import com.example.ui.screens.editor.EditorViewModel
import com.example.ui.screens.export.ExportScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.luts.LutsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.tools.ToolsScreen
import kotlinx.coroutines.launch

@Composable
fun OgooNavHost(
    preferences: ThemePreferences,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current

    // Physical small screen detection
    val isPhysicalSmallScreen = configuration.screenHeightDp < 720 || configuration.screenWidthDp < 380
    val isCompactActive = preferences.compactMode || isPhysicalSmallScreen

    // Calculate dynamic UI scaling factor so the entire UI resizes proportionally for small display phones
    val autoScale = if (isPhysicalSmallScreen) {
        val widthRatio = configuration.screenWidthDp / 390f
        val heightRatio = configuration.screenHeightDp / 720f
        minOf(widthRatio, heightRatio).coerceIn(0.80f, 1.0f)
    } else {
        1.0f
    }

    // When compact mode is enabled, scale UI to ~83% (or smaller if phone display is tiny)
    val scaleFactor = when {
        preferences.compactMode -> 0.83f
        isPhysicalSmallScreen -> autoScale
        else -> 1.0f
    }

    val customDensity = remember(baseDensity.density, baseDensity.fontScale, scaleFactor) {
        Density(
            density = baseDensity.density * scaleFactor,
            fontScale = baseDensity.fontScale * scaleFactor
        )
    }

    val projectRepository = remember { ProjectRepository(context) }
    val editorViewModel: EditorViewModel = viewModel()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    // Clean workspace with real user photos (remove any demo data)
    LaunchedEffect(Unit) {
        projectRepository.clearSampleProjects()
    }

    val isDark = when (preferences.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // Determine active bottom nav destination
    val currentNavDestination = when (currentScreen) {
        Screen.Home -> NavDestination.HOME
        Screen.Edit -> NavDestination.EDIT
        Screen.Luts -> NavDestination.LUTS
        Screen.Tools -> NavDestination.TOOLS
        Screen.Export -> NavDestination.EXPORT
        else -> null
    }

    // Global back handling
    if (currentScreen != Screen.Home && currentScreen != Screen.Splash) {
        BackHandler {
            currentScreen = Screen.Home
        }
    }

    CompositionLocalProvider(LocalDensity provides customDensity) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen Content Container
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    Screen.Splash -> {
                        SplashScreen(
                            onGetStarted = { currentScreen = Screen.Home }
                        )
                    }
                    Screen.Home -> {
                        HomeScreen(
                            onOpenProject = { projectId ->
                                editorViewModel.loadProject(projectId)
                                currentScreen = Screen.Edit
                            },
                            onImportUri = { uri ->
                                editorViewModel.loadFromUri(uri)
                                currentScreen = Screen.Edit
                            },
                            onImportBitmap = { bmp ->
                                editorViewModel.loadFromBitmap(bmp)
                                currentScreen = Screen.Edit
                            },
                            onOpenSettings = { currentScreen = Screen.Settings },
                            onToggleDarkLightMode = {
                                scope.launch { settingsRepository.toggleMode() }
                            },
                            onToggleCompactMode = {
                                scope.launch {
                                    val next = !preferences.compactMode
                                    settingsRepository.setCompactMode(next)
                                    Toast.makeText(
                                        context,
                                        if (next) "Compact Mode: ON (UI Resized)" else "Standard Display Mode Restored",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            isDark = isDark,
                            compactModePreference = isCompactActive
                        )
                    }
                    Screen.Edit -> {
                        EditorScreen(
                            viewModel = editorViewModel,
                            onBack = { currentScreen = Screen.Home },
                            onOpenLuts = { currentScreen = Screen.Luts },
                            onToggleCompactMode = {
                                scope.launch {
                                    val next = !preferences.compactMode
                                    settingsRepository.setCompactMode(next)
                                    Toast.makeText(
                                        context,
                                        if (next) "Compact Mode: ON (UI Resized)" else "Standard Display Mode Restored",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            isDark = isDark,
                            compactModePreference = isCompactActive
                        )
                    }
                    Screen.Luts -> {
                        LutsScreen(
                            viewModel = editorViewModel,
                            onBack = { currentScreen = Screen.Edit },
                            isDark = isDark,
                            compactModePreference = isCompactActive
                        )
                    }
                    Screen.Tools -> {
                        ToolsScreen(
                            viewModel = editorViewModel,
                            onBack = { currentScreen = Screen.Edit },
                            isDark = isDark,
                            compactModePreference = isCompactActive
                        )
                    }
                    Screen.Export -> {
                        ExportScreen(
                            viewModel = editorViewModel,
                            onBack = { currentScreen = Screen.Edit },
                            onOpenSettings = { currentScreen = Screen.Settings },
                            isDark = isDark,
                            compactModePreference = isCompactActive
                        )
                    }
                    Screen.Settings -> {
                        SettingsScreen(
                            preferences = preferences,
                            settingsRepository = settingsRepository,
                            onBack = { currentScreen = Screen.Home },
                            isDark = isDark,
                            compactModePreference = isCompactActive
                        )
                    }
                }
            }

            // Floating Bottom Navigation Bar
            if (currentNavDestination != null) {
                FloatingNavBar(
                    currentDestination = currentNavDestination,
                    onNavigate = { dest ->
                        currentScreen = when (dest) {
                            NavDestination.HOME -> Screen.Home
                            NavDestination.EDIT -> Screen.Edit
                            NavDestination.LUTS -> Screen.Luts
                            NavDestination.TOOLS -> Screen.Tools
                            NavDestination.EXPORT -> Screen.Export
                        }
                    },
                    isDark = isDark,
                    glassIntensity = preferences.glassIntensity,
                    isCompact = isCompactActive,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
