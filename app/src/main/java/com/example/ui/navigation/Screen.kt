package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Edit : Screen("edit")
    data object Luts : Screen("luts")
    data object Tools : Screen("tools")
    data object Export : Screen("export")
    data object Settings : Screen("settings")
}
