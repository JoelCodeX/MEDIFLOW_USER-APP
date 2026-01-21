package com.jotadev.mediflow.screens.recursos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecursosScreen(
    onResourceClick: (String, String, String) -> Unit
) {
    val viewModel: RecursosViewModel = viewModel(factory = RecursosViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val recursos by viewModel.items.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecursos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray.copy(0.2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (uiState) {
            is RecursosUiState.Loading -> {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator()
            }
            is RecursosUiState.Error -> {
                val message = (uiState as RecursosUiState.Error).message
                Spacer(Modifier.height(24.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            else -> Unit
        }

        Spacer(Modifier.height(8.dp))

        recursos.forEachIndexed { index, item ->
            ResourceCard(
                item = item,
                onClick = {
                    val url = item.url
                    if (!url.isNullOrBlank()) {
                        viewModel.registrarInteraccionVista(item.id)
                        onResourceClick(url, item.tipo ?: "documento", item.title)
                    }
                }
            )
            if (index != recursos.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResourceCard(
    item: RecursoUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (item.tipo) {
                    "pdf" -> Icons.Rounded.PictureAsPdf
                    "video" -> Icons.Rounded.PlayCircle
                    "documento" -> Icons.Rounded.Description
                    "tip" -> Icons.Rounded.Policy
                    "audio" -> Icons.Rounded.Audiotrack
                    else -> Icons.Rounded.Description
                },
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                if (!item.desc.isNullOrBlank()) {
                    Text(
                        item.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Box(contentAlignment = Alignment.Center) {
                val progress = if (item.completed) 1f else 0f
                ResourceDonut(progress = progress, color = MaterialTheme.colorScheme.primary)
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
