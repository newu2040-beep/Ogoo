package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppTheme
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePreferences

fun getThemeColorScheme(theme: AppTheme, isDark: Boolean, customAccent: Long) = when (theme) {
    AppTheme.OBSIDIAN -> if (isDark) {
        darkColorScheme(
            primary = ObsidianAccentCyan,
            secondary = ObsidianSecondaryCyan,
            background = ObsidianBackgroundDark,
            surface = ObsidianSurfaceDark,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0284C7),
            secondary = Color(0xFF0EA5E9),
            background = Color(0xFFF1F5F9),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight
        )
    }
    AppTheme.CLOUD -> if (isDark) {
        darkColorScheme(
            primary = Color(0xFF38BDF8),
            secondary = Color(0xFF7DD3FC),
            background = Color(0xFF0A0F1D),
            surface = Color(0xFF131B2E),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark
        )
    } else {
        lightColorScheme(
            primary = CloudAccentBlue,
            secondary = Color(0xFF38BDF8),
            background = CloudBackgroundLight,
            surface = CloudSurfaceLight,
            onPrimary = Color.White,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight
        )
    }
    AppTheme.MINT -> if (isDark) {
        darkColorScheme(
            primary = MintAccent,
            secondary = MintSecondary,
            background = MintBackgroundDark,
            surface = MintSurfaceDark,
            onPrimary = Color.Black,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark
        )
    } else {
        lightColorScheme(
            primary = MintAccent,
            secondary = MintSecondary,
            background = Color(0xFFF0FDF4),
            surface = Color.White,
            onPrimary = Color.White,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight
        )
    }
    AppTheme.AURORA -> if (isDark) {
        darkColorScheme(
            primary = AuroraAccentPurple,
            secondary = AuroraSecondaryPink,
            background = AuroraBackgroundDark,
            surface = AuroraSurfaceDark,
            onPrimary = Color.White,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark
        )
    } else {
        lightColorScheme(
            primary = AuroraAccentPurple,
            secondary = AuroraSecondaryPink,
            background = Color(0xFFFAF5FF),
            surface = Color.White,
            onPrimary = Color.White,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight
        )
    }
    AppTheme.ROSE -> if (isDark) {
        darkColorScheme(
            primary = RoseAccent,
            secondary = RoseSecondary,
            background = RoseBackgroundDark,
            surface = RoseSurfaceDark,
            onPrimary = Color.White,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark
        )
    } else {
        lightColorScheme(
            primary = RoseAccent,
            secondary = RoseSecondary,
            background = Color(0xFFFFF1F2),
            surface = Color.White,
            onPrimary = Color.White,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight
        )
    }
    AppTheme.CUSTOM -> {
        val accent = Color(customAccent)
        if (isDark) {
            darkColorScheme(
                primary = accent,
                secondary = accent.copy(alpha = 0.8f),
                background = Color(0xFF0B0F17),
                surface = Color(0xFF141A26),
                onPrimary = Color.Black,
                onBackground = TextPrimaryDark,
                onSurface = TextPrimaryDark
            )
        } else {
            lightColorScheme(
                primary = accent,
                secondary = accent.copy(alpha = 0.8f),
                background = Color(0xFFF8FAFC),
                surface = Color.White,
                onPrimary = Color.White,
                onBackground = TextPrimaryLight,
                onSurface = TextPrimaryLight
            )
        }
    }
}

@Composable
fun OgooTheme(
    preferences: ThemePreferences = ThemePreferences(),
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (preferences.mode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = getThemeColorScheme(
        theme = preferences.theme,
        isDark = isDark,
        customAccent = preferences.customAccentColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
