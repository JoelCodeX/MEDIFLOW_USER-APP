package com.jotadev.mediflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay

data class HorarioItem(
    val hora: String,
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector,
    val color: Color = Color.Gray,
    val horaReal: LocalTime? = null
)

@Composable
fun CardHorario(
    items: List<HorarioItem> = emptyList(),
    modifier: Modifier = Modifier,
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(30_000) // Actualizar cada 30 segundos
        }
    }

    Card(
        modifier = modifier.fillMaxWidth().border(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(16.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (items.isEmpty()) {
                Text(
                    text = "Horario aún no asignado",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.forEachIndexed { index, item ->
                    val nextItem = items.getOrNull(index + 1)
                    HorarioRow(
                        item = item,
                        nextItemTime = nextItem?.horaReal,
                        currentTime = currentTime,
                        isFirst = index == 0,
                        isLast = index == items.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun HorarioRow(
    item: HorarioItem,
    nextItemTime: LocalTime?,
    currentTime: LocalTime,
    isFirst: Boolean,
    isLast: Boolean
) {
    val isReached = item.horaReal != null && !currentTime.isBefore(item.horaReal)
    
    // Cálculo del progreso de la línea
    val progress = remember(currentTime, item.horaReal, nextItemTime) {
        if (item.horaReal == null || nextItemTime == null) 0f
        else if (currentTime.isBefore(item.horaReal)) 0f
        else if (!currentTime.isBefore(nextItemTime)) 1f
        else {
            val total = ChronoUnit.MINUTES.between(item.horaReal, nextItemTime).toFloat()
            val current = ChronoUnit.MINUTES.between(item.horaReal, currentTime).toFloat()
            if (total <= 0) 1f else (current / total).coerceIn(0f, 1f)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.width(36.dp)
        ) {
            LeadingIcon(
                icon = item.icono,
                contentDescription = item.titulo,
                isReached = isReached,
                activeColor = item.color
            )
            if (!isLast) {
                // Línea de tiempo personalizada con progreso
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(2.dp) // Un poco más ancha para ver el relleno
                        .padding(top = 4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant) // Fondo gris
                ) {
                    // Relleno dinámico
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(progress)
                            .background(item.color) // Color del item actual llena la línea hacia el siguiente
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isReached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                
                if (isFirst) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isReached) item.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.hora,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isReached) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Text(
                        text = item.hora,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (isReached) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            Text(
                text = item.subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun LeadingIcon(
    icon: ImageVector,
    contentDescription: String,
    isReached: Boolean,
    activeColor: Color
) {
    val bg = if (isReached) activeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isReached) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isReached) activeColor else Color.Transparent

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = fg,
            modifier = Modifier.size(20.dp)
        )
    }
}