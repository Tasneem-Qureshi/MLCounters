package com.mlcounters.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Dark Gaming Theme Colors
val ElectricBlue = Color(0xFF1E88E5)
val CyberPurple = Color(0xFF7C4DFF)
val NeonOrange = Color(0xFFFF6D00)

val DeepBlack = Color(0xFF0D0D0D)
val DarkGraySurface = Color(0xFF1A1A2E)
val SurfaceVariantDark = Color(0xFF16213E)

val TextWhite = Color(0xFFFFFFFF)
val TextLightGray = Color(0xFFE2E8F0)
val TextMutedGray = Color(0xFF94A3B8)
val BorderDark = Color(0xFF2E3856)

val ErrorRed = Color(0xFFCF6679)

// Accent Colors for Hero Roles
val RoleTank = Color(0xFF4FC3F7)
val RoleFighter = Color(0xFFFF7043)
val RoleAssassin = Color(0xFFAB47BC)
val RoleMage = Color(0xFF42A5F5)
val RoleMarksman = Color(0xFFFFA726)
val RoleSupport = Color(0xFF66BB6A)

// Mastery Colors
val MasteryNotPlayed = Color(0xFF757575)
val MasteryBeginner = Color(0xFFCD7F32)     // Bronze
val MasteryIntermediate = Color(0xFFC0C0C0) // Silver
val MasteryExpert = Color(0xFFFFD700)       // Gold
val MasteryMaster = Color(0xFFB9F2FF)       // Diamond

// Counter Score Bar Colors
val CounterScoreHigh = Color(0xFF4CAF50)    // Strong counter (8-10)
val CounterScoreMedium = Color(0xFFFFB300)  // Moderate counter (5-7)
val CounterScoreLow = Color(0xFFE53935)     // Weak counter (1-4)

fun getRoleColor(role: String): Color {
    return when (role.trim().lowercase()) {
        "tank" -> RoleTank
        "fighter" -> RoleFighter
        "assassin" -> RoleAssassin
        "mage" -> RoleMage
        "marksman" -> RoleMarksman
        "support" -> RoleSupport
        else -> ElectricBlue
    }
}

fun getMasteryColor(level: Int): Color {
    return when (level) {
        1 -> MasteryBeginner
        2 -> MasteryIntermediate
        3 -> MasteryExpert
        4 -> MasteryMaster
        else -> MasteryNotPlayed
    }
}

fun getCounterScoreColor(score: Int): Color {
    return when {
        score >= 8 -> CounterScoreHigh
        score >= 5 -> CounterScoreMedium
        else -> CounterScoreLow
    }
}
