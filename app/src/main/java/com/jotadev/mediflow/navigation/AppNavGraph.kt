package com.jotadev.mediflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jotadev.mediflow.screens.login.LoginScreen
import com.jotadev.mediflow.screens.register.RegisterScreen

@Composable
fun AppNavGraph(startDestination: String = "login") {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginClick = { navController.navigate("home") }
            )
        }
        composable("register") {
            RegisterScreen(
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }
        composable("home") {
            HomeRoot(onLogoutClick = { navController.navigateToLoginAfterLogout() })
        }
    }
}