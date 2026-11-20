package com.mlcounters.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mlcounters.app.ui.screens.counter.CounterScreen
import com.mlcounters.app.ui.screens.heroes.HeroDetailScreen
import com.mlcounters.app.ui.screens.heroes.HeroListScreen
import com.mlcounters.app.ui.screens.home.HomeScreen
import com.mlcounters.app.ui.screens.items.ItemScreen
import com.mlcounters.app.ui.screens.mastery.MasteryScreen
import com.mlcounters.app.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    onToggleOverlay: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHeroDetail = { heroId ->
                    navController.navigate(Screen.HeroDetail.createRoute(heroId))
                },
                onToggleOverlay = onToggleOverlay
            )
        }

        composable(Screen.HeroList.route) {
            HeroListScreen(
                onNavigateToHeroDetail = { heroId ->
                    navController.navigate(Screen.HeroDetail.createRoute(heroId))
                }
            )
        }

        composable(
            route = Screen.HeroDetail.route,
            arguments = listOf(
                navArgument("heroId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) {
            HeroDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHeroDetail = { nextHeroId ->
                    navController.navigate(Screen.HeroDetail.createRoute(nextHeroId))
                }
            )
        }

        composable(Screen.CounterMatrix.route) {
            CounterScreen(
                onNavigateToHeroDetail = { heroId ->
                    navController.navigate(Screen.HeroDetail.createRoute(heroId))
                }
            )
        }

        composable(Screen.ItemGuide.route) {
            ItemScreen(
                onNavigateToHeroDetail = { heroId ->
                    navController.navigate(Screen.HeroDetail.createRoute(heroId))
                }
            )
        }

        composable(Screen.Mastery.route) {
            MasteryScreen(
                onNavigateToHeroDetail = { heroId ->
                    navController.navigate(Screen.HeroDetail.createRoute(heroId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
