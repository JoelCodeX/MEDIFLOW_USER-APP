package com.jotadev.mediflow.navigation

import androidx.navigation.NavController

fun NavController.navigateToHomeClearStack() {
    navigate(NavRoute.Home.route) {
        popUpTo(NavRoute.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateToLoginFromRegisterClearStack() {
    navigate(NavRoute.Login.route) {
        popUpTo(NavRoute.Register.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateToLoginAfterLogout() {
    navigate(NavRoute.Login.route) {
        popUpTo(NavRoute.Home.route) { inclusive = true }
        launchSingleTop = true
    }
}