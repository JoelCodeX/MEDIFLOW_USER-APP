package com.jotadev.mediflow.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor,
        titleContentColor = contentColor,
        actionIconContentColor = contentColor,
        navigationIconContentColor = contentColor,
    )

    val currentActions by rememberUpdatedState(newValue = actions)
    val currentNavigationIcon by rememberUpdatedState(newValue = navigationIcon)

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = { currentNavigationIcon?.invoke() },
        actions = currentActions,
        colors = colors,
        modifier = modifier,
    )
}
@Composable
fun TopBarForNav(
    navController: NavController,
    routeTitleMapper: (String?) -> String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    centered: Boolean = true,
) {
    // Observa la BackStackEntry actual y deriva el título.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val title = routeTitleMapper(route)

    TopBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions
    )
}