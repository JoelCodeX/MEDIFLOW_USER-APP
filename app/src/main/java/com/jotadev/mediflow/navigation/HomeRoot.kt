package com.jotadev.mediflow.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.ArrowBack
import com.jotadev.mediflow.core.network.ApiClient
import com.jotadev.mediflow.core.network.ApiService
import com.jotadev.mediflow.screens.citas.CitasScreen
import com.jotadev.mediflow.screens.home.HomeScreen
import com.jotadev.mediflow.screens.mensajes.MensajesScreen
import com.jotadev.mediflow.screens.perfil.PerfilScreen
import com.jotadev.mediflow.screens.recursos.RecursosScreen
import com.jotadev.mediflow.ui.components.TopBarForNav
import com.jotadev.mediflow.ui.components.TopBar
import com.jotadev.mediflow.ui.components.ModalAsistencia
import com.jotadev.mediflow.ui.components.AsistenciaConfig
import com.jotadev.mediflow.ui.components.AsistenciaEstado
import com.jotadev.mediflow.ui.components.AsistenciaModo
import com.jotadev.mediflow.screens.encuestas.EncuestasScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun HomeRoot(onLogoutClick: () -> Unit) {
    val navController = rememberNavController()
    val homeViewModel: com.jotadev.mediflow.screens.home.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.jotadev.mediflow.screens.home.HomeViewModel.Factory)
    var showModal by remember { mutableStateOf(false) }
    var encuestaExitRequests by remember { mutableStateOf(0) }
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
        containerColor = MaterialTheme.colorScheme.onPrimary,
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val route = navBackStackEntry?.destination?.route
            if (route == "encuestas") {
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
                    },
                    actions = {
                        if (route == "inicio") {
                            Text(
                                "marcar asistencia",
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp, 20.dp, 0.dp, 20.dp))
                                    .background(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    showModal = true
                                    homeViewModel.refreshAsistenciaEstado()
                                }, modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onPrimary.copy(0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DoneOutline,
                                    contentDescription = "Marcar asistencia"
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val route = navBackStackEntry?.destination?.route
            if (route != "encuestas") {
                NavigationBar {
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
                                selectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
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
                onLogoutClick = onLogoutClick,
                onPendingEncuestaClick = { navController.navigate("encuestas") }
            )
        }
            composable("recursos") { RecursosScreen() }
            composable("mensajes") { MensajesScreen() }
            composable("citas") { CitasScreen() }
            composable("perfil") { PerfilScreen() }
            composable("encuestas") { EncuestasScreen(onFinished = { navController.popBackStack() }, exitRequests = encuestaExitRequests) }
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