package com.jotadev.mediflow.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jotadev.mediflow.screens.login.LoginScreen
import com.jotadev.mediflow.screens.register.RegisterScreen

fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(NavRoute.Login.route) {
        LoginScreen(
            onRegisterClick = { navController.navigate(NavRoute.Register.route) },
            onLoginClick = { navController.navigateToHomeClearStack() }
        )
    }

    composable(NavRoute.Register.route) {
        RegisterScreen(
            onLoginClick = { navController.navigateToLoginFromRegisterClearStack() }
        )
    }
}