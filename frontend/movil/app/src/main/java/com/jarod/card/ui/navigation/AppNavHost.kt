package com.jarod.card.ui.navigation

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jarod.card.features.auth.LoginScreen
import com.jarod.card.features.friends.FriendsScreen
import com.jarod.card.features.game.GameScreen
import com.jarod.card.features.game.cardskin.CardDesignScreen
import com.jarod.card.features.lobby.LobbyScreen

object AppRoute {
    const val LOGIN = "login"
    const val LOBBY = "lobby"
    const val FRIENDS = "friends"
    const val SETTINGS = "settings"
    const val GAME = "game/{roomId}"
    fun game(roomId: String) = "game/$roomId"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.LOBBY,
        modifier = modifier.safeDrawingPadding()
    ) {
        composable(AppRoute.LOGIN) {
            LoginScreen()
        }
        composable(AppRoute.FRIENDS) {
            FriendsScreen()
        }
        composable(AppRoute.LOBBY) {
            LobbyScreen(
                onOpenGame = { roomId -> navController.navigate(AppRoute.game(roomId)) },
                onOpenSettings = { navController.navigate(AppRoute.SETTINGS) }
            )
        }
        composable(AppRoute.SETTINGS) {
            CardDesignScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = AppRoute.GAME,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId").orEmpty()
            GameScreen(roomId = roomId)
        }
    }
}
