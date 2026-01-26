package com.jotadev.mediflow.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.mediflow.ui.components.CardCalendario
import com.jotadev.mediflow.ui.components.CardEncuestaPendiente
import com.jotadev.mediflow.ui.components.CardHorario
import com.jotadev.mediflow.ui.components.IndicadoresPanel
import com.jotadev.mediflow.utils.PendingSurveyManager

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onPendingEncuestaClick: () -> Unit = {},
    onAsistenciaClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val uiState = homeViewModel.uiState.collectAsState().value
    val state = homeViewModel.state.collectAsState().value
    val refreshing = uiState is HomeUiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { homeViewModel.refreshAll() }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Bienvenido, ${state.userName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¿Cómo te sientes hoy?",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onAsistenciaClick,
                        modifier = Modifier
                            .size(42.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(50)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = Icons.Rounded.CheckCircle,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Marcar asistencia"
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
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
                   CardCalendario {  }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Metricas de Hoy",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "Ver Reporte",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            textAlign = TextAlign.End
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
                    Row {
                        Text(
                            text = "Tu Horario de Hoy",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                    }
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
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}