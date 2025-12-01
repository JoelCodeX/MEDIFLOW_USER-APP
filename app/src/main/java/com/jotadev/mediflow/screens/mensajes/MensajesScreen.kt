package com.jotadev.mediflow.screens.mensajes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MensajesScreen() {
    val search = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Mensajes",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tu bandeja de mensajes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = search.value,
            onValueChange = { search.value = it },
            label = { Text("Buscar mensajes") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
            )
        )
        Spacer(Modifier.height(12.dp))

        MessageCard(
            remitente = "Recursos Humanos",
            asunto = "Actualización de políticas internas",
            preview = "Estimado equipo, adjuntamos el documento con las nuevas políticas...",
            fecha = "Hoy 09:12"
        )
        Spacer(Modifier.height(8.dp))
        MessageCard(
            remitente = "Sistemas",
            asunto = "Mantenimiento programado",
            preview = "El sistema estará en mantenimiento este fin de semana...",
            fecha = "Ayer 18:45"
        )
        Spacer(Modifier.height(8.dp))
        MessageCard(
            remitente = "Dirección Médica",
            asunto = "Reunión de coordinación",
            preview = "Se convoca a reunión el martes a las 8:00 AM...",
            fecha = "Lun 10:22"
        )
    }
}

@Composable
private fun MessageCard(
    remitente: String,
    asunto: String,
    preview: String,
    fecha: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(remitente, style = MaterialTheme.typography.titleMedium)
                Text(fecha, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(asunto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(preview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { /* TODO abrir */ }) { Text("Abrir") }
                TextButton(onClick = { /* TODO archivar */ }) { Text("Archivar") }
            }
        }
    }
}