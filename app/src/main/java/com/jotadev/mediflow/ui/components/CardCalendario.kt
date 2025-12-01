package com.jotadev.mediflow.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DiaCalendario(
    val fecha: LocalDate,
    val dia: Int,
    val diaSemana: String,
    val esHoy: Boolean = false,
    val esSeleccionado: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CardCalendario(
    modifier: Modifier = Modifier,
    onDiaSeleccionado: (LocalDate) -> Unit = {}
) {
    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    var diaSeleccionado by remember { mutableStateOf<LocalDate?>(null) }
    val hoy = LocalDate.now()
    val listState = rememberLazyListState()

    val diasDelMes = remember(mesActual) {
        val primerDia = mesActual.atDay(1)
        val ultimoDia = mesActual.atEndOfMonth()
        
        (1..ultimoDia.dayOfMonth).map { dia ->
            val fecha = mesActual.atDay(dia)
            DiaCalendario(
                fecha = fecha,
                dia = dia,
                diaSemana = fecha.dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.SHORT, 
                    Locale("es", "ES")
                ).uppercase(),
                esHoy = fecha == hoy,
                esSeleccionado = fecha == diaSeleccionado
            )
        }
    }

    LaunchedEffect(mesActual) {
        if (mesActual == YearMonth.now()) {
            val indiceDiaHoy = diasDelMes.indexOfFirst { it.esHoy }
            if (indiceDiaHoy != -1) {
                listState.animateScrollToItem(maxOf(0, indiceDiaHoy - 2))
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mesActual.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
                    ).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row {
                    IconButton(
                        onClick = { mesActual = mesActual.minusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Mes anterior",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { mesActual = mesActual.plusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mes siguiente",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(diasDelMes) { dia ->
                    DiaItem(
                        dia = dia,
                        onClick = {
                            diaSeleccionado = dia.fecha
                            onDiaSeleccionado(dia.fecha)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaItem(
    dia: DiaCalendario,
    onClick: () -> Unit
) {
    val indicatorColor = when {
        dia.esHoy -> MaterialTheme.colorScheme.primary
        dia.esSeleccionado -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val onIndicatorColor = when {
        dia.esHoy -> MaterialTheme.colorScheme.onPrimary
        dia.esSeleccionado -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color = indicatorColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val nombreDia = dia.diaSemana
                .replace(".", "")
                .lowercase()
                .replaceFirstChar { it.uppercase() }

            Text(
                text = nombreDia,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = onIndicatorColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = dia.dia.toString().padStart(2, '0'),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (dia.esHoy) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = onIndicatorColor,
                textAlign = TextAlign.Center
            )
        }
    }
}