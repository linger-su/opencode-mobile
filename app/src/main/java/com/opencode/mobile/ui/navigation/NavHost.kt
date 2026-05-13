package com.opencode.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.opencode.mobile.ui.chat.ChatScreen
import com.opencode.mobile.ui.log.LogScreen
import com.opencode.mobile.ui.settings.SettingsScreen

object Routes {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val LOG = "log"
}

@Composable
fun OpenCodeNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CHAT
    ) {
        composable(Routes.CHAT) {
            ChatScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToLog = { navController.navigate(Routes.LOG) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.LOG) {
            LogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
