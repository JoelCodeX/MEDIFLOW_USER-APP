package com.jotadev.mediflow.screens.recursos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecursosScreen(
    viewModel: RecursosViewModel = viewModel(factory = RecursosViewModel.Factory),
    onResourceClick: (String, String, String, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recursos by viewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    LaunchedEffect(Unit) {
        viewModel.loadRecursos()
    }

    val filteredRecursos = remember(recursos, searchQuery, selectedCategory) {
        recursos.filter { item ->
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                    (item.desc?.contains(searchQuery, ignoreCase = true) == true)
            val matchesCategory = when (selectedCategory) {
                "Todos" -> true
                "Video" -> item.tipo.equals("video", ignoreCase = true) || item.url?.endsWith(".mp4", ignoreCase = true) == true
                "Audio" -> item.tipo.equals("audio", ignoreCase = true) || item.url?.endsWith(".mp3", ignoreCase = true) == true || item.url?.endsWith(".wav", ignoreCase = true) == true || item.url?.endsWith(".ogg", ignoreCase = true) == true
                "Imagen" -> item.tipo.equals("imagen", ignoreCase = true) || item.url?.endsWith(".jpg", ignoreCase = true) == true || item.url?.endsWith(".png", ignoreCase = true) == true || item.url?.endsWith(".jpeg", ignoreCase = true) == true
                "Pdf" -> item.tipo.equals("pdf", ignoreCase = true) || item.url?.endsWith(".pdf", ignoreCase = true) == true
                "Tips" -> item.tipo.equals("tip", ignoreCase = true)
                "Documentos" -> item.tipo.equals("documento", ignoreCase = true) && item.url?.endsWith(".mp3", ignoreCase = true) != true && item.url?.endsWith(".mp4", ignoreCase = true) != true && item.url?.endsWith(".jpg", ignoreCase = true) != true && item.url?.endsWith(".png", ignoreCase = true) != true
                else -> false
            }

            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar Mejorada
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )
        Spacer(modifier = Modifier.height(16.dp)) // Más espacio tras búsqueda

        // Categories
        CategoryFilters(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header "Recientes"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recientes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
            Text(
                text = "Ver todo",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.clickable { /* Handle Ver todo */ }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is RecursosUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecursosUiState.Error -> {
                    val message = (uiState as RecursosUiState.Error).message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(filteredRecursos) { _, item ->
                            NewResourceCard(
                                item = item,
                                onClick = {
                                    val url = item.url
                                    if (!url.isNullOrBlank()) {
                                        viewModel.registrarInteraccionVista(item.id)
                                        onResourceClick(
                                            url,
                                            item.tipo ?: "documento",
                                            item.title,
                                            item.id
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Buscar recursos...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp) // Altura fija más compacta
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp) // Bordes redondeados pero no full circle para mantener estilo
            )
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun CategoryFilters(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val iconcategories = listOf(
        Icons.Rounded.GridView,
        Icons.Rounded.PlayCircle,
        Icons.Rounded.Headphones,
        Icons.Rounded.Image,
        Icons.Rounded.PictureAsPdf,
        Icons.Rounded.Lightbulb,
        Icons.AutoMirrored.Rounded.Article
    )
    val categories = listOf("Todos", "Video", "Audio", "Imagen", "Pdf", "Tips", "Documentos")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.zip(iconcategories)) { (category, icon) ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Color.White else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun NewResourceCard(
    item: RecursoUi,
    onClick: () -> Unit
) {
    val normalizedType = if (item.tipo.equals("documento", ignoreCase = true)) {
        when {
            item.url?.endsWith(".mp3", ignoreCase = true) == true -> "Audio"
            item.url?.endsWith(".mp4", ignoreCase = true) == true -> "Video"
            item.url?.endsWith(".pdf", ignoreCase = true) == true -> "Pdf"
            item.url?.endsWith(".jpg", ignoreCase = true) == true -> "Imagen"
            item.url?.endsWith(".png", ignoreCase = true) == true -> "Imagen"
            else -> "Documento"
        }
    } else {
        item.tipo?.capitalize() ?: "Documento"
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .height(IntrinsicSize.Min),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Simulation
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getThumbnailColor(normalizedType)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForType(normalizedType),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                // Overlay Icon (small corner icon) - optional, mimicking the image
                if (normalizedType.equals("pdf", ignoreCase = true)) {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$normalizedType • ${item.desc ?: ""}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                val progress = item.progress
                
                when {
                    progress >= 1f -> {
                        // Completado
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981), // Green
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Completado",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = "100%",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFFE5E7EB),
                        )
                    }
                    progress > 0f -> {
                        // En curso
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "En curso",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color(0xFFE5E7EB),
                        )
                    }
                    else -> {
                        // Sin comenzar
                        val isMedia = item.tipo?.lowercase() in listOf("video", "mp4", "audio", "mp3")
                        if (isMedia) {
                            Button(
                                onClick = onClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .align(Alignment.End),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    text = "Comenzar",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else {
                            Text(
                                text = "Sin comenzar",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color.Transparent,
                                trackColor = Color(0xFFE5E7EB),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getIconForType(tipo: String?): ImageVector {
    val t = tipo?.lowercase() ?: ""
    return when {
        t == "video" || t == "mp4" || t.endsWith("mp4") -> Icons.Rounded.PlayCircle
        t == "audio" || t == "mp3" || t.endsWith("mp3") -> Icons.Rounded.Audiotrack
        t == "pdf" || t.endsWith("pdf") -> Icons.Rounded.PictureAsPdf
        t == "tip" -> Icons.Rounded.Lightbulb
        t == "imagen" || t.endsWith("jpg") || t.endsWith("png") -> Icons.Rounded.Image
        else -> Icons.AutoMirrored.Rounded.Article
    }
}

private fun getThumbnailColor(tipo: String?): Color {
    val t = tipo?.lowercase() ?: ""
    return when {
        t == "video" || t == "mp4" || t.endsWith("mp4") -> Color(0xFFE0F2FE) // Light Blue
        t == "audio" || t == "mp3" || t.endsWith("mp3") -> Color(0xFFF3E8FF) // Light Purple
        t == "pdf" || t.endsWith("pdf") -> Color(0xFFFEE2E2) // Light Red
        t == "tip" -> Color(0xFFFEF3C7) // Light Yellow/Amber
        t == "imagen" || t.endsWith("jpg") || t.endsWith("png") -> Color(0xFFFCE7F3) // Light Pink
        else -> Color(0xFFE5E7EB) // Gray
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

