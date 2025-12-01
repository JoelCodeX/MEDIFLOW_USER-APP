package com.jotadev.mediflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CardEncuestaPendiente(
    titulo: String = "Tienes una encuesta pendiente",
    subtitulo: String? = null,
    modifier: Modifier = Modifier,
    onResponder: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFA726), // naranja
                            Color(0xFFFF7043)  // coral
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = "Encuesta Pendiente",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (subtitulo != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onResponder,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFFF7043)),
                    modifier = Modifier.size(width = 120.dp, height = 40.dp),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(text = "Responder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}