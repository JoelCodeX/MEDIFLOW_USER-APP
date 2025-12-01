package com.jotadev.mediflow.ui.components

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AsistenciaModo { ENTRADA, SALIDA }

data class AsistenciaConfig(
    val workplaceLat: Double,
    val workplaceLon: Double,
    val workplaceRadiusMeters: Float = 100f,
    val turnoNombre: String = "Turno"
)

data class AsistenciaEstado(
    val ultimaEntradaMillis: Long? = null,
    val ultimaSalidaMillis: Long? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalAsistencia(
    visible: Boolean,
    modoInicial: AsistenciaModo,
    config: AsistenciaConfig,
    estado: AsistenciaEstado,
    onDismiss: () -> Unit,
    onConfirm: (modo: AsistenciaModo, timestampMillis: Long, location: Location?, distanceMeters: Float?) -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var modo by remember { mutableStateOf(modoInicial) }
    // Sincroniza el modo cuando cambia el modoInicial externo
    LaunchedEffect(modoInicial) {
        modo = modoInicial
    }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var distanceMeters by remember { mutableStateOf<Float?>(null) }
    var hasLocationPermission by remember { mutableStateOf(checkLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) @androidx.annotation.RequiresPermission(anyOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            val loc = getLastKnownLocation(context)
            currentLocation = loc
            distanceMeters = loc?.let { distanceToWorkplace(it, config) }
        }
    }

    LaunchedEffect(Unit) {
        // Actualiza la hora actual cada segundo
        while (true) {
            nowMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    val dentroDeZona = distanceMeters?.let { it <= config.workplaceRadiusMeters } ?: false

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onPrimary)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado
            Text(
                text = if (modo == AsistenciaModo.ENTRADA) "Marcar entrada" else "Marcar salida",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = config.turnoNombre,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Hora actual y últimas marcas
            HoraActualSection(nowMillis = nowMillis, estado = estado)

            // Estado de ubicación
            UbicacionSection(
                hasPermission = hasLocationPermission,
                dentroDeZona = dentroDeZona,
                distanceMeters = distanceMeters,
                onSolicitarUbicacion = @androidx.annotation.RequiresPermission(anyOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        val loc = getLastKnownLocation(context)
                        currentLocation = loc
                        distanceMeters = loc?.let { distanceToWorkplace(it, config) }
                    }
                }
            )

            // Acciones
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { modo = AsistenciaModo.ENTRADA },
                    enabled = modo != AsistenciaModo.ENTRADA
                ) { Text("Entrada") }
                Button(
                    onClick = { modo = AsistenciaModo.SALIDA },
                    enabled = modo != AsistenciaModo.SALIDA
                ) { Text("Salida") }
            }

            val puedeConfirmar = true
            val ayuda: String? = null

            if (ayuda != null) {
                Text(
                    text = ayuda,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val ts = System.currentTimeMillis()
                    onConfirm(modo, ts, currentLocation, distanceMeters)
                    onDismiss()
                },
                enabled = puedeConfirmar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (modo == AsistenciaModo.ENTRADA) "Confirmar entrada" else "Confirmar salida")
            }

            // Info adicional
            InfoExtraSection(distanceMeters = distanceMeters, config = config)
        }
    }
}

@Composable
private fun HoraActualSection(nowMillis: Long, estado: AsistenciaEstado) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Hora actual",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatTime(nowMillis),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Última entrada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = estado.ultimaEntradaMillis?.let { formatTime(it) } ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Última salida", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = estado.ultimaSalidaMillis?.let { formatTime(it) } ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun UbicacionSection(
    hasPermission: Boolean,
    dentroDeZona: Boolean,
    distanceMeters: Float?,
    onSolicitarUbicacion: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Ubicación",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val color = when {
                !hasPermission -> MaterialTheme.colorScheme.tertiary
                dentroDeZona -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            }
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
            val texto = when {
                !hasPermission -> "Sin permisos de ubicación"
                dentroDeZona -> "Dentro del lugar de trabajo"
                else -> "Fuera del lugar de trabajo"
            }
            Text(text = texto, style = MaterialTheme.typography.bodyMedium)
        }
        if (distanceMeters != null) {
            Text(
                text = "Distancia: ${"%.1f".format(distanceMeters)} m",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(onClick = onSolicitarUbicacion) { Text("Obtener ubicación") }
    }
}

@Composable
private fun InfoExtraSection(distanceMeters: Float?, config: AsistenciaConfig) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Información",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Radio permitido: ${config.workplaceRadiusMeters.toInt()} m",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (distanceMeters != null) {
            val estado = if (distanceMeters <= config.workplaceRadiusMeters) "OK" else "Fuera"
            Text(
                text = "Validación: $estado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
    return fine || coarse
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun getLastKnownLocation(context: Context): Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    for (p in providers) {
        try {
            val loc = lm.getLastKnownLocation(p)
            if (loc != null) return loc
        } catch (_: SecurityException) {
            return null
        }
    }
    return null
}

private fun distanceToWorkplace(loc: Location, config: AsistenciaConfig): Float {
    val result = FloatArray(1)
    Location.distanceBetween(
        loc.latitude, loc.longitude,
        config.workplaceLat, config.workplaceLon,
        result
    )
    return result[0]
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(millis: Long): String {
    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    val fmt = DateTimeFormatter.ofPattern("hh:mm a")
    return dt.format(fmt)
}