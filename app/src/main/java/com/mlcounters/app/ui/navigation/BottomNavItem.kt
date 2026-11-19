package com.mlcounters.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        icon = Icons.Default.Home,
        label = "Home"
    ),
    BottomNavItem(
        route = Screen.HeroList.route,
        icon = Icons.Default.Person,
        label = "Heroes"
    ),
    BottomNavItem(
        route = Screen.CounterMatrix.route,
        icon = Icons.Default.CompareArrows,
        label = "Counters"
    ),
    BottomNavItem(
        route = Screen.ItemGuide.route,
        icon = Icons.Default.Shield,
        label = "Items"
    ),
    BottomNavItem(
        route = Screen.Mastery.route,
        icon = Icons.Default.Star,
        label = "Mastery"
    )
)
