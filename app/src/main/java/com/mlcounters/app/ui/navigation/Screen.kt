package com.mlcounters.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object HeroList : Screen("hero_list")
    data object HeroDetail : Screen("hero_detail/{heroId}") {
        fun createRoute(heroId: Int): String = "hero_detail/$heroId"
    }
    data object CounterMatrix : Screen("counter_matrix")
    data object ItemGuide : Screen("item_guide")
    data object Mastery : Screen("mastery")
    data object Settings : Screen("settings")
}
