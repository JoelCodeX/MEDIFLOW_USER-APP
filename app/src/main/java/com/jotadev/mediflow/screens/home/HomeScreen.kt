package com.jotadev.mediflow.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.mediflow.screens.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import com.jotadev.mediflow.ui.components.CardBienvenida
import com.jotadev.mediflow.ui.components.CardCalendario
import com.jotadev.mediflow.ui.components.CardHorario
import com.jotadev.mediflow.ui.components.HorarioItem
import com.jotadev.mediflow.ui.components.IndicadoresPanel
import androidx.compose.runtime.getValue
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.platform.LocalContext
import com.jotadev.mediflow.ui.components.CardEncuestaPendiente
import com.jotadev.mediflow.utils.PendingSurveyManager

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(onLogoutClick: () -> Unit, onPendingEncuestaClick: () -> Unit = {}) {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val uiState = homeViewModel.uiState.collectAsState().value
    val state = homeViewModel.state.collectAsState().value
    val refreshing = uiState is HomeUiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { homeViewModel.refreshAll() }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
            .background(color = Color.Gray.copy(0.2f))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
        item {
            CardBienvenida(
                titulo = "Bienvenido, ${state.userName}",
                subtitulo = "¿Cómo te sientes hoy?",
                gradientColors = listOf(
                    Color(0xFF059669),  // Verde más oscuro
                    Color(0xFF10B981), // Verde esmeralda
                )
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Inserta aviso de encuesta pendiente justo debajo de Bienvenida
        if (PendingSurveyManager.isPending(context)) {
            item {
                CardEncuestaPendiente(onResponder = { onPendingEncuestaClick() })
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
        item {
            IndicadoresPanel(
                bienestarPromedio = state.bienestarPromedio,
                saludEmocionalProm7 = state.saludEmocionalProm7,
                asistenciaSemanalPct = state.asistenciaSemanalPct,
                motivacionGeneral = state.motivacionGeneral,
                loading = (uiState is HomeUiState.Loading)
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item { 
            CardHorario(items = state.horarioItems)
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item { CardCalendario {  } }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            // Botón de "Marcar asistencia" removido; ya existe en la TopBar
        }
        item {
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogoutClick()
                },
                modifier = Modifier.padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )
    }
    
}