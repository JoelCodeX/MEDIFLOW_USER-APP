package com.jotadev.mediflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class IndicadorData(
    val titulo: String,
    val descripcion: String,
    val valorActual: Float,
    val valorMaximo: Float,
    val unidad: String,
    val icon: ImageVector? = null,
    val umbrales: IndicadorUmbrales = IndicadorUmbrales(),
)

data class IndicadorUmbrales(
    val warning: Float = 0.4f,
    val ok: Float = 0.7f
)

enum class IndicadorNivel { OK, WARNING, CRITICAL }

private fun nivelPorRatio(ratio: Float, u: IndicadorUmbrales): IndicadorNivel = when {
    ratio >= u.ok -> IndicadorNivel.OK
    ratio >= u.warning -> IndicadorNivel.WARNING
    else -> IndicadorNivel.CRITICAL
}

@Composable
fun DonutIndicator(
    progressRatio: Float,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    strokeWidth: Dp = 9.dp,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    progressColor: Color = MaterialTheme.colorScheme.secondary,
    center: (@Composable () -> Unit)? = null,
) {
    val sweep = remember(progressRatio) { progressRatio.coerceIn(0f, 1f) * 360f }
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = stroke
            )
        }
        center?.invoke()
    }
}

@Composable
fun IndicadorCard(
    data: IndicadorData,
    modifier: Modifier = Modifier,
) {
    val ratio = if (data.valorMaximo > 0f) (data.valorActual / data.valorMaximo).coerceIn(0f, 1f) else 0f
    val nivel = nivelPorRatio(ratio, data.umbrales)
    val colorNivel = when (nivel) {
        IndicadorNivel.OK -> MaterialTheme.colorScheme.secondary
        IndicadorNivel.WARNING -> Color(0xFFFFA726)
        IndicadorNivel.CRITICAL -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.padding(12.dp)) {
            val donutSize = when {
                maxWidth < 140.dp -> 60.dp
                maxWidth < 160.dp -> 70.dp
                maxWidth < 180.dp -> 80.dp
                else -> 90.dp
            }
            val stroke = when {
                maxWidth < 140.dp -> 6.dp
                maxWidth < 160.dp -> 7.dp
                maxWidth < 180.dp -> 8.dp
                else -> 9.dp
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (data.icon != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorNivel.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = data.icon, 
                                contentDescription = null, 
                                tint = colorNivel,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DonutIndicator(
                        progressRatio = ratio,
                        size = donutSize,
                        strokeWidth = stroke,
                        progressColor = colorNivel
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${formatNumber(data.valorActual)} ${data.unidad}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Text(
                    text = data.descripcion,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LeyendaNivel(nivel = nivel)
                }
            }
        }
    }
}

@Composable
private fun LeyendaNivel(nivel: IndicadorNivel) {
    val (texto, color) = when (nivel) {
        IndicadorNivel.OK -> "Óptimo" to MaterialTheme.colorScheme.secondary
        IndicadorNivel.WARNING -> "Advertencia" to Color(0xFFFFA726)
        IndicadorNivel.CRITICAL -> "Crítico" to MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(texto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ShimmerBox(
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    highlightColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "shimmer-shift"
    )
    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(shift.value - 200f, 0f),
        end = Offset(shift.value, 0f)
    )
    Box(modifier = modifier.background(brush))
}

@Composable
private fun IndicadorCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            ShimmerBox(modifier = Modifier.height(18.dp).fillMaxWidth(0.6f))
            // Donut placeholder
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ShimmerBox(modifier = Modifier.size(90.dp).clip(CircleShape))
            }
            // Descripción
            ShimmerBox(modifier = Modifier.height(12.dp).fillMaxWidth(0.8f))
            Spacer(modifier = Modifier.weight(1f))
            // Leyenda
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ShimmerBox(modifier = Modifier.size(10.dp).clip(CircleShape))
                ShimmerBox(modifier = Modifier.height(12.dp).fillMaxWidth(0.3f))
            }
        }
    }
}

@Composable
private fun IndicadoresPanelSkeleton(modifier: Modifier = Modifier) {
    val filas = listOf(listOf(1, 2), listOf(3, 4))
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filas.forEach { filaItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filaItems.forEach { _ ->
                    IndicadorCardSkeleton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
fun IndicadoresPanel(
    bienestarPromedio: Float,
    saludEmocionalProm7: Float,
    asistenciaSemanalPct: Float,
    motivacionGeneral: Float,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (loading) {
        IndicadoresPanelSkeleton(modifier)
        return
    }
    val items = listOf(
        IndicadorData(
            titulo = "Nivel de bienestar",
            descripcion = "Promedio diario (estrés, ánimo, sueño)",
            valorActual = bienestarPromedio,
            valorMaximo = 100f,
            unidad = "%",
            icon = Icons.Outlined.SelfImprovement
        ),
        IndicadorData(
            titulo = "Salud emocional",
            descripcion = "Promedio de las últimas 7 evaluaciones de ánimo",
            valorActual = saludEmocionalProm7,
            valorMaximo = 100f,
            unidad = "%",
            icon = Icons.Outlined.Favorite
        ),
        IndicadorData(
            titulo = "Asistencia",
            descripcion = "Porcentaje de puntualidad semanal",
            valorActual = asistenciaSemanalPct,
            valorMaximo = 100f,
            unidad = "%",
            icon = Icons.Outlined.Schedule
        ),
        IndicadorData(
            titulo = "Motivación general",
            descripcion = "Promedio ponderado (motivación + asistencia + estrés)",
            valorActual = motivacionGeneral,
            valorMaximo = 100f,
            unidad = "%",
            icon = Icons.Outlined.Spa
        )
    )
    val filas = remember(items) { items.chunked(2) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filas.forEach { filaItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filaItems.forEach { item ->
                    IndicadorCard(
                        data = item,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                if (filaItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun formatNumber(n: Float): String {
    return if (n % 1f == 0f) n.toInt().toString() else String.format("%.1f", n)
}

@Preview
@Composable
fun IndicadoresPanelPreview() {
    IndicadoresPanel(
        bienestarPromedio = 76.5f,
        saludEmocionalProm7 = 62.0f,
        asistenciaSemanalPct = 88.0f,
        motivacionGeneral = 54.3f,
        loading = false,
        modifier = Modifier.padding(16.dp)
    )
}