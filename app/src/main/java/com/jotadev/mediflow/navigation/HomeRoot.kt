package com.jotadev.mediflow.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jotadev.mediflow.screens.citas.CitasScreen
import com.jotadev.mediflow.screens.encuestas.EncuestasScreen
import com.jotadev.mediflow.screens.home.HomeScreen
import com.jotadev.mediflow.screens.mensajes.MensajesScreen
import com.jotadev.mediflow.screens.perfil.PerfilScreen
import com.jotadev.mediflow.screens.recursos.RecursosScreen
import com.jotadev.mediflow.screens.recursos.VisorRecursoScreen
import com.jotadev.mediflow.ui.components.AsistenciaConfig
import com.jotadev.mediflow.ui.components.AsistenciaEstado
import com.jotadev.mediflow.ui.components.AsistenciaModo
import com.jotadev.mediflow.ui.components.ModalAsistencia
import com.jotadev.mediflow.ui.components.TopBar
import com.jotadev.mediflow.ui.components.TopBarForNav
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeRoot(onLogoutClick: () -> Unit) {
    val navController = rememberNavController()
    val homeViewModel: com.jotadev.mediflow.screens.home.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.jotadev.mediflow.screens.home.HomeViewModel.Factory)
    val recursosViewModel: com.jotadev.mediflow.screens.recursos.RecursosViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.jotadev.mediflow.screens.recursos.RecursosViewModel.Factory)

    var showModal by remember { mutableStateOf(false) }
    var encuestaExitRequests by remember { mutableIntStateOf(0) }
    val state = homeViewModel.state.collectAsState().value
    val asistenciaEstado: AsistenciaEstado = state.asistenciaEstado
    val asistenciaConfig = remember {
        AsistenciaConfig(
            workplaceLat = 0.0,
            workplaceLon = 0.0,
            workplaceRadiusMeters = 100f,
            turnoNombre = "Hoy"
        )
    }

    val tabs = listOf(
        BottomNavItem("Inicio", "inicio", Icons.Rounded.Home),
        BottomNavItem("Recursos", "recursos", Icons.Rounded.Description),
        BottomNavItem("Mensajes", "mensajes", Icons.Rounded.Forum),
        BottomNavItem("Citas", "citas", Icons.Rounded.Event),
        BottomNavItem("Perfil", "perfil", Icons.Rounded.Person)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val route = navBackStackEntry?.destination?.route
            if (route != "inicio") {
                if (route?.startsWith("visor") == true) {
                    val titulo = navBackStackEntry?.arguments?.getString("titulo") ?: "Recurso"
                    TopBar(
                        title = titulo,
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Atrás"
                                )
                            }
                        }
                    )
                } else if (route == "encuestas") {
                    TopBar(
                        title = "Encuestas",
                        navigationIcon = {
                            IconButton(onClick = { encuestaExitRequests++ }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Atrás"
                                )
                            }
                        }
                    )
                } else {
                    TopBarForNav(
                        navController = navController,
                        routeTitleMapper = { rt ->
                            when (rt) {
                                "inicio" -> "Inicio"
                                "recursos" -> "Recursos"
                                "mensajes" -> "Mensajes"
                                "citas" -> "Citas"
                                "perfil" -> "Perfil"
                                else -> "MediFlow"
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val route = navBackStackEntry?.destination?.route
            if (route != "encuestas" && route?.startsWith("visor") != true) {
                Column {
                    Divider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        val currentDestination: NavDestination? = navBackStackEntry?.destination
                        tabs.forEach { item ->
                            val selected =
                                currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = "inicio"
        ) {
        composable("inicio") {
            HomeScreen(
                onPendingEncuestaClick = { navController.navigate("encuestas") },
                onAsistenciaClick = {
                    showModal = true
                    homeViewModel.refreshAsistenciaEstado()
                }
            )
        }
            composable("recursos") { 
                RecursosScreen(
                    viewModel = recursosViewModel,
                    onResourceClick = { url, tipo, titulo, id ->
                        val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                        val encodedTipo = URLEncoder.encode(tipo, StandardCharsets.UTF_8.toString())
                        val encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8.toString())
                        navController.navigate("visor?url=$encodedUrl&tipo=$encodedTipo&titulo=$encodedTitulo&id=$id")
                    }
                ) 
            }
            composable("mensajes") { MensajesScreen() }
            composable("citas") { CitasScreen() }
            composable("perfil") { PerfilScreen(onLogoutClick = onLogoutClick) }
            composable("encuestas") { EncuestasScreen(onFinished = { navController.popBackStack() }, exitRequests = encuestaExitRequests) }
            composable(
                route = "visor?url={url}&tipo={tipo}&titulo={titulo}&id={id}",
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType; defaultValue = "" },
                    navArgument("tipo") { type = NavType.StringType; defaultValue = "" },
                    navArgument("titulo") { type = NavType.StringType; defaultValue = "" },
                    navArgument("id") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: -1
                VisorRecursoScreen(
                    url = backStackEntry.arguments?.getString("url") ?: "",
                    tipo = backStackEntry.arguments?.getString("tipo") ?: "",
                    titulo = backStackEntry.arguments?.getString("titulo") ?: "Recurso",
                    onBack = { navController.popBackStack() },
                    onComplete = {
                        if (id != -1) {
                            recursosViewModel.registrarInteraccionCompletado(id)
                            // Refrescar lista al volver?
                            recursosViewModel.loadRecursos()
                        }
                    }
                )
            }
        }
    }

    if (showModal) {
        val scope = rememberCoroutineScope()
        val hasActiveEntrada = asistenciaEstado.ultimaEntradaMillis != null && (
            asistenciaEstado.ultimaSalidaMillis == null ||
                (asistenciaEstado.ultimaEntradaMillis ?: 0L) > (asistenciaEstado.ultimaSalidaMillis ?: Long.MIN_VALUE)
        )
        ModalAsistencia(
            visible = true,
            modoInicial = if (hasActiveEntrada) AsistenciaModo.SALIDA else AsistenciaModo.ENTRADA,
            config = asistenciaConfig,
            estado = asistenciaEstado,
            onDismiss = { showModal = false },
            onConfirm = { modo, _, location, dist ->
                if (modo == AsistenciaModo.ENTRADA) {
                    homeViewModel.marcarEntrada(location, dist)
                    // Navegar siempre a encuestas; la pantalla mostrará estado vacío si no hay pendientes
                    navController.navigate("encuestas")
                } else {
                    homeViewModel.marcarSalida()
                }
                homeViewModel.refreshAsistenciaEstado()
                showModal = false
            }
        )
    }
}
