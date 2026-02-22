package com.explosion.messenger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.explosion.messenger.ui.screens.chat.ChatScreen
import com.explosion.messenger.ui.screens.chat.ChatViewModel
import com.explosion.messenger.ui.screens.login.AuthViewModel
import com.explosion.messenger.ui.screens.login.LoginScreen
import com.explosion.messenger.ui.screens.chat.MessageScreen
import com.explosion.messenger.ui.screens.chat.MessageViewModel
import com.explosion.messenger.ui.screens.settings.SettingsScreen
import com.explosion.messenger.ui.screens.settings.SettingsViewModel
import com.explosion.messenger.util.TokenManager
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

@Composable
fun NavGraph(tokenManager: TokenManager) {
    val navController = rememberNavController()
    val startDestination = if (tokenManager.getToken() != null) "chat" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("login") {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.context.startService(android.content.Intent(navController.context, com.explosion.messenger.services.NeuralLinkService::class.java))
                    navController.navigate("chat") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            val viewModel: com.explosion.messenger.ui.screens.login.RegisterViewModel = hiltViewModel()
            com.explosion.messenger.ui.screens.login.RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    // Registration finishes, go to login to use the freshly made account
                    navController.popBackStack()
                }
            )
        }
        composable("chat") {
            val viewModel: ChatViewModel = hiltViewModel()
            ChatScreen(
                viewModel = viewModel,
                onChatClick = { chatId ->
                    navController.navigate("message/$chatId")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.context.stopService(android.content.Intent(navController.context, com.explosion.messenger.services.NeuralLinkService::class.java))
                    tokenManager.clearToken()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            "message/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.IntType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId") ?: 0
            val viewModel: MessageViewModel = hiltViewModel()
            MessageScreen(
                viewModel = viewModel,
                chatId = chatId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
