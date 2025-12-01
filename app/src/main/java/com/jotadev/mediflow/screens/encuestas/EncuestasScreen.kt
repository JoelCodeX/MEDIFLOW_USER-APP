package com.jotadev.mediflow.screens.encuestas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.mediflow.core.network.PreguntaDto
import com.jotadev.mediflow.screens.home.HomeViewModel
import com.jotadev.mediflow.utils.PendingSurveyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncuestasScreen(onFinished: () -> Unit, exitRequests: Int = 0) {
    val vm: EncuestasViewModel = viewModel()
    val homeVm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val state = homeVm.state.collectAsState().value
    val usuarioId = state.usuarioId
    val ui = vm.state.collectAsState().value

    LaunchedEffect(usuarioId) {
        usuarioId?.let { vm.loadPending(it) }
    }

    // Manejo de salida con confirmación si la encuesta no está completa
    val encuestaCurrent = ui.current
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    // También intercepta el botón de regresar de la TopBar
    LaunchedEffect(exitRequests) {
        if (exitRequests > 0) {
            val complete = isEncuestaCompletada(encuestaCurrent, ui.answers)
            if (!complete) {
                showExitDialog = true
            } else {
                onFinished()
            }
        }
    }

    BackHandler {
        val complete = isEncuestaCompletada(encuestaCurrent, ui.answers)
        if (!complete) {
            showExitDialog = true
        } else {
            onFinished()
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.onPrimary,
            title = {
                Text(
                    "Encuesta obligatoria",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            text = {
                Text(
                    "Es obligatorio responder la encuesta antes de salir. ¿Deseas posponerla?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = encuestaCurrent?.id?.toString()
                    PendingSurveyManager.setPending(context, true, id)
                    showExitDialog = false
                    onFinished()
                }) {
                    Text(
                        "ACEPTAR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(
                        "CANCELAR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray.copy(0.2f))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (usuarioId == null) {
            Text(
                "No se pudo identificar el usuario.",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            return@Column
        }
        if (ui.loading) {
            CircularProgressIndicator()
            return@Column
        }
        val encuesta = ui.current
        if (encuesta == null && !ui.loading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Sin encuestas",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "No tienes encuestas programadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
        } else if (encuesta != null) {
            // Header Card con clasificación y resumen
            HeaderEncuestaCard(
                titulo = encuesta.titulo,
                descripcion = encuesta.descripcion,
                tipo = encuesta.tipo ?: "general",
                totalPreguntas = encuesta.preguntas?.size ?: 0
            )
            Spacer(Modifier.height(8.dp))
            encuesta.preguntas?.forEach { pregunta ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        PreguntaItem(
                            pregunta = pregunta,
                            value = ui.answers[pregunta.id] ?: "",
                            onChange = { v -> vm.setAnswer(pregunta.id, v) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            if (encuesta.respondida_hoy == true) {
                Text(
                    text = "Ya respondiste esta encuesta hoy",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Button(
                onClick = {
                    vm.submit(usuarioId) { ok ->
                        if (ok && vm.state.value.current == null) {
                            PendingSurveyManager.clear(context)
                            onFinished()
                        }
                    }
                },
                enabled = (
                    encuesta.respondida_hoy != true &&
                    (encuesta.preguntas?.all { ui.answers.containsKey(it.id) } == true)
                )
            ) {
                Text(
                    "Enviar respuestas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Verifica si todas las preguntas tienen una respuesta no vacía
private fun isEncuestaCompletada(
    encuesta: com.jotadev.mediflow.core.network.EncuestaDto?,
    answers: Map<Int, String>
): Boolean {
    val preguntas = encuesta?.preguntas ?: emptyList<PreguntaDto>()
    if (preguntas.isEmpty()) return false
    return preguntas.all { pregunta ->
        val v = answers[pregunta.id] ?: ""
        v.isNotBlank()
    }
}

@Composable
private fun HeaderEncuestaCard(
    titulo: String,
    descripcion: String?,
    tipo: String,
    totalPreguntas: Int,
    gradientColors: List<Color> = listOf(
        Color(0xFF3742D8),
        Color(0xFF181F72)
    ),
) {
    val resolvedTipo = normalizeTipo(tipo) ?: inferTipoFromText(titulo, descripcion)
    val (icon, tint, label) = tipoIconAndLabel(resolvedTipo)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        titulo, style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                descripcion?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "${totalPreguntas} pregunta(s)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        },
                        shape = RoundedCornerShape(100),
                        border = BorderStroke(0.dp, Color.Transparent),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(
                                alpha = 0.3f
                            )
                        )
                    )
                }
            }
        }

    }
}

// Normaliza distintos valores de backend/admin a claves internas
private fun normalizeTipo(raw: String?): String? {
    if (raw == null) return null
    return when (raw.trim().lowercase()) {
        "fisico", "físico", "physical" -> "fisico"
        "psicosocial", "emocional", "psicosocial_emocional", "psicoemocional" -> "psicosocial_emocional"
        "social", "organizacional", "clima", "clima laboral", "clima_laboral" -> "clima_laboral"
        "respuesta_corta", "short", "short_answer", "checkin_diario" -> "respuesta_corta"
        "general", "checkin", "encuesta" -> "general"
        else -> raw.lowercase()
    }
}

// Fallback por contenido cuando 'tipo' no está bien configurado en la encuesta
private fun inferTipoFromText(titulo: String, descripcion: String?): String {
    val t = ("$titulo ${descripcion ?: ""}").lowercase()
    return when {
        listOf(
            "fisico",
            "físico",
            "actividad",
            "ejercicio",
            "salud"
        ).any { t.contains(it) } -> "fisico"

        listOf(
            "psicosocial",
            "emocional",
            "estrés",
            "estres",
            "motivación",
            "motivacion",
            "apoyo"
        ).any { t.contains(it) } -> "psicosocial_emocional"

        listOf(
            "social",
            "clima",
            "carga laboral",
            "organizacional",
            "equipo"
        ).any { t.contains(it) } -> "social"

        else -> "general"
    }
}

@Composable
private fun tipoIconAndLabel(tipo: String): Triple<androidx.compose.ui.graphics.vector.ImageVector, Color, String> {
    return when (tipo.lowercase()) {
        "fisico" -> Triple(
            Icons.Outlined.MedicalInformation,
            MaterialTheme.colorScheme.primary,
            "Físico"
        )

        "psicosocial", "emocional", "psicosocial_emocional" -> Triple(
            Icons.Outlined.FavoriteBorder,
            MaterialTheme.colorScheme.tertiary,
            "Psicosocial"
        )

        "clima_laboral" -> Triple(
            Icons.Outlined.Group,
            MaterialTheme.colorScheme.secondary,
            "Clima laboral"
        )

        "respuesta_corta" -> Triple(
            Icons.Outlined.Info,
            MaterialTheme.colorScheme.secondary,
            "Respuesta corta"
        )

        else -> Triple(
            Icons.Outlined.Assessment,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "General"
        )
    }
}

@Composable
private fun PreguntaItem(pregunta: PreguntaDto, value: String, onChange: (String) -> Unit) {
    // Mostrar chip de clasificación por pregunta
    val categoriaKey = categoriaPorPreguntaTipo(pregunta.tipo)
    val (_, categoriaTint, categoriaLabel) = tipoIconAndLabel(categoriaKey)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            pregunta.texto,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.weight(1f)
        )
        AssistChip(
            onClick = {},
            label = {
                Text(
                    categoriaLabel, style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondary.copy(0.3f)
                )
            },
            shape = RoundedCornerShape(100),
            border = BorderStroke(0.dp, Color.Transparent),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
    when (pregunta.tipo.lowercase()) {
        "texto" -> {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
        }

        "multiple" -> {
            val opciones = pregunta.opciones ?: emptyList()
            var selected by remember(value) { mutableStateOf(value) }
            Column {
                opciones.forEach { opt ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selected == opt,
                            onClick = {
                                selected = opt
                                onChange(opt)
                            }
                        )
                        Text(
                            opt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }

        "likert" -> {
            val opciones = pregunta.opciones ?: listOf(
                "Muy en desacuerdo", "En desacuerdo", "Neutral", "De acuerdo", "Muy de acuerdo"
            )
            var selected by remember(value) { mutableStateOf(value) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                opciones.forEach { opt ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = selected == opt,
                            onClick = {
                                selected = opt
                                onChange(opt)
                            }
                        )
                        Text(
                            opt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }

        else -> {
            Text("Tipo de pregunta no soportado: ${pregunta.tipo}")
        }
    }
}

// Clasificación por tipo de respuesta de la pregunta (fallback cuando no hay categoría explícita)
private fun categoriaPorPreguntaTipo(tipoPregunta: String): String {
    return when (tipoPregunta.lowercase()) {
        "texto" -> "respuesta_corta"
        // Para escalas y opciones, usar Clima laboral como categoría general
        "likert", "multiple" -> "clima_laboral"
        else -> "clima_laboral"
    }
}