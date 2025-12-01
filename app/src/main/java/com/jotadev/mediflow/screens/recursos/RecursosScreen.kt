package com.jotadev.mediflow.screens.recursos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RecursosScreen() {
    val recursos = remember {
        listOf(
            ResourceItem(
                type = ResourceType.PDF,
                title = "Políticas internas",
                desc = "Lineamientos actualizados para el personal",
                progress = 0.85f,
                completed = false
            ),
            ResourceItem(
                type = ResourceType.DOC,
                title = "Manual del colaborador",
                desc = "Guía de procesos y buenas prácticas",
                progress = 1f,
                completed = true
            ),
            ResourceItem(
                type = ResourceType.VIDEO,
                title = "Capacitación inicial",
                desc = "Video introductorio de estándares de atención",
                progress = 0.4f,
                completed = false
            ),
            ResourceItem(
                type = ResourceType.AUDIO,
                title = "Podcast de seguridad",
                desc = "Buenas prácticas de bioseguridad",
                progress = 0.6f,
                completed = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray.copy(0.2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Recursos",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Materiales educativos: PDFs, videos y audios",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(16.dp))
        recursos.forEachIndexed { index, item ->
            ResourceCard(item = item)
            if (index != recursos.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResourceCard(item: ResourceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (item.type) {
                    ResourceType.PDF -> Icons.Rounded.PictureAsPdf
                    ResourceType.DOC -> Icons.Rounded.Description
                    ResourceType.VIDEO -> Icons.Rounded.PlayCircle
                    ResourceType.AUDIO -> Icons.Rounded.Audiotrack
                },
                contentDescription = null
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(item.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondary)
            }

            Box(contentAlignment = Alignment.Center) {
                ResourceDonut(progress = item.progress, color = MaterialTheme.colorScheme.primary)
                if (item.completed) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.alpha(0.9f)
                    )
                }
            }
        }
    }
}

private enum class ResourceType { PDF, DOC, VIDEO, AUDIO }

private data class ResourceItem(
    val type: ResourceType,
    val title: String,
    val desc: String,
    val progress: Float, // 0f..1f visualización/apertura
    val completed: Boolean
)

@Composable
private fun ResourceDonut(progress: Float, color: Color) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = clamped,
            color = color,
            trackColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f),
            strokeWidth = 6.dp
        )
        Text(
            text = "${(clamped * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}