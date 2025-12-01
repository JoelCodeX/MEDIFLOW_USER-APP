package com.jotadev.mediflow.screens.home

import android.util.Log
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.jotadev.mediflow.core.events.AppEvents
import com.jotadev.mediflow.core.network.ApiClient
import com.jotadev.mediflow.core.network.ApiService
import com.jotadev.mediflow.core.network.HorarioDto
import com.jotadev.mediflow.core.network.UsuarioDto
import com.jotadev.mediflow.core.network.MarcarEntradaRequest
import com.jotadev.mediflow.core.network.RegistrarSalidaRequest
import com.jotadev.mediflow.core.network.AsistenciaActualDto
import android.location.Location
import android.os.Build
import com.jotadev.mediflow.ui.components.AsistenciaEstado
import com.jotadev.mediflow.ui.components.HorarioItem
import com.jotadev.mediflow.di.AppModule
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data object Ready : HomeUiState
    data class Error(val message: String) : HomeUiState
}

data class HomeState(
    val userName: String = "",
    val horarioItems: List<HorarioItem> = emptyList(),
    val usuarioId: Int? = null,
    val asistenciaEstado: AsistenciaEstado = AsistenciaEstado(),
    // Indicadores para el Home (0-100)
    val bienestarPromedio: Float = 0f,
    val saludEmocionalProm7: Float = 0f,
    val asistenciaSemanalPct: Float = 0f,
    val motivacionGeneral: Float = 0f
)

class HomeViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val api: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private var cachedUsuarioId: Int? = null
    private val authRepository = AppModule.authRepository

    init {
        Log.d(TAG, "HomeViewModel inicializado")
        loadHome()
        // Escuchar eventos de push notifications
        viewModelScope.launch {
            Log.d(TAG, "Iniciando escucha de eventos AppEvents")
            AppEvents.events.collect { event ->
                Log.d(TAG, "Evento recibido: $event")
                if (event == "horario_asignado") {
                    Log.d(TAG, "Refrescando horarios por notificación push")
                    fetchHorarios()
                }
            }
        }
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Log.d(TAG, "Usuario autenticado: ${user.displayName} (UID: ${user.uid})")
                _state.value = _state.value.copy(userName = user.displayName ?: "Usuario")
                
                // Suscribirse al topic FCM del usuario
                val topic = "user_${user.uid}"
                Log.d(TAG, "Suscribiéndose al topic FCM: $topic")
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "✅ Suscripción exitosa al topic: $topic")
                        } else {
                            Log.e(TAG, "❌ Error suscribiéndose al topic: $topic", task.exception)
                        }
                    }
                
                fetchHorarios()
                // Intentar resolver usuarioId para acciones de asistencia
                resolveUsuarioId()
            } else {
                Log.e(TAG, "Usuario no autenticado")
                _uiState.value = HomeUiState.Error("Usuario no autenticado")
            }
        }
    }

    fun refreshAll() {
        // Forzar estado de carga y refrescar horarios + estado de asistencia
        _uiState.value = HomeUiState.Loading
        fetchHorarios()
        // Re-resolver usuarioId por si cambió y refrescar estado
        resolveUsuarioId()
    }

    private fun fetchHorarios() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    _uiState.value = HomeUiState.Error("Usuario no autenticado")
                    return@launch
                }
                Log.d(TAG, "Obteniendo horarios para UID: $uid")
                val resp = api.getHorariosByUid(uid, vigente = true)
                if (resp.isSuccessful) {
                    val horarios = resp.body() ?: emptyList()
                    Log.d(TAG, "Horarios obtenidos: ${horarios.size} elementos")
                    val items = horariosToItems(horarios)
                    _state.value = _state.value.copy(horarioItems = items)
                    _uiState.value = HomeUiState.Ready
                } else {
                    Log.w(TAG, "Usuario sin UID, no se pueden obtener horarios")
                    _state.value = _state.value.copy(horarioItems = emptyList())
                    _uiState.value = HomeUiState.Ready
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo horarios", e)
                _state.value = _state.value.copy(horarioItems = emptyList())
                _uiState.value = HomeUiState.Error(e.message ?: "Error cargando horarios")
            }
        }
    }

    private fun horariosToItems(horarios: List<HorarioDto>): List<HorarioItem> {
        // Por simplicidad, tomar el primer horario vigente
        val h = horarios.firstOrNull() ?: return emptyList()
        val fmtIn = DateTimeFormatter.ofPattern("HH:mm")
        val fmtOut = DateTimeFormatter.ofPattern("h:mm a")
        val entrada = LocalTime.parse(h.hora_entrada, fmtIn).format(fmtOut)
        val salida = LocalTime.parse(h.hora_salida, fmtIn).format(fmtOut)
        val refrigerio = h.hora_refrigerio?.let { LocalTime.parse(it, fmtIn).format(fmtOut) }
        val items = mutableListOf<HorarioItem>()
        items.add(HorarioItem(hora = entrada, titulo = "Entrada", subtitulo = "Comenzar jornada", icono = androidx.compose.material.icons.Icons.Outlined.PlayCircle))
        if (refrigerio != null) {
            items.add(HorarioItem(hora = "$refrigerio", titulo = "Refrigerio", subtitulo = "Hora de break", icono = androidx.compose.material.icons.Icons.Outlined.Group))
        }
        items.add(HorarioItem(hora = salida, titulo = "Salida", subtitulo = "Cerrar jornada", icono = androidx.compose.material.icons.Icons.Outlined.ExitToApp))
        return items
    }

    fun resolveUsuarioId() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                // Primer intento: resolver usuario directamente
                val resp = api.getUsuarioByUid(uid)
                if (resp.isSuccessful) {
                    val u: UsuarioDto? = resp.body()
                    val id = u?.id
                    cachedUsuarioId = id
                    _state.value = _state.value.copy(usuarioId = id)
                    refreshAsistenciaEstado()
                    refreshIndicadores()
                    return@launch
                }

                // Si no existe (404) o fallo, intentar sincronizar el token Firebase y reintentar una vez
                val code = resp.code()
                Log.w(TAG, "Fallo obteniendo usuario por UID (code=$code). Intentando re-sync y reintento...")
                val currentUser = FirebaseAuth.getInstance().currentUser
                val idToken = try {
                    currentUser?.getIdToken(true)?.await()?.token
                } catch (e: Exception) {
                    Log.e(TAG, "Error obteniendo idToken de Firebase", e)
                    null
                }
                if (idToken.isNullOrBlank()) {
                    Log.w(TAG, "idToken vacío; no se puede sincronizar con backend")
                    return@launch
                }
                val synced = try {
                    authRepository.syncFirebaseToken(idToken)
                } catch (e: Exception) {
                    Log.e(TAG, "Error sincronizando token con backend", e)
                    false
                }
                if (!synced) {
                    Log.w(TAG, "Sincronización backend no exitosa; se mantiene usuarioId sin resolver")
                    return@launch
                }
                // Reintento de resolución
                val retry = api.getUsuarioByUid(uid)
                if (retry.isSuccessful) {
                    val u2: UsuarioDto? = retry.body()
                    val id2 = u2?.id
                    cachedUsuarioId = id2
                    _state.value = _state.value.copy(usuarioId = id2)
                    refreshAsistenciaEstado()
                    refreshIndicadores()
                } else {
                    Log.w(TAG, "Reintento de obtener usuario por UID falló: ${retry.code()} ${retry.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resolviendo usuarioId", e)
            }
        }
    }

    fun refreshAsistenciaEstado() {
        viewModelScope.launch {
            try {
                val id = cachedUsuarioId ?: _state.value.usuarioId ?: return@launch
                val resp = api.getAsistenciaActual(id)
                if (resp.isSuccessful) {
                    val a: AsistenciaActualDto? = resp.body()
                    val nowDate = java.time.LocalDate.now()
                    val entradaMillis = a?.hora_entrada?.let {
                        try {
                            val lt = LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                            java.time.ZonedDateTime.of(nowDate, lt, java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        } catch (e: Exception) { null }
                    }
                    val salidaMillis = a?.hora_salida?.let {
                        try {
                            val lt = LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                            java.time.ZonedDateTime.of(nowDate, lt, java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        } catch (e: Exception) { null }
                    }
                    _state.value = _state.value.copy(asistenciaEstado = AsistenciaEstado(entradaMillis, salidaMillis))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refrescando estado de asistencia", e)
            }
        }
    }

    fun marcarEntrada(location: Location?, distanceMeters: Float?) {
        viewModelScope.launch {
            try {
                val id = cachedUsuarioId ?: _state.value.usuarioId
                if (id == null) {
                    resolveUsuarioId()
                    return@launch
                }
                val ubic = location?.let { "${it.latitude},${it.longitude};dist=${distanceMeters ?: 0f}m" }
                val device = "Android ${Build.MODEL} (${Build.VERSION.RELEASE})"
                val body = MarcarEntradaRequest(id_usuario = id, ubicacion_marcado = ubic, dispositivo_marcado = device)
                val resp = api.marcarEntrada(body)
                if (resp.isSuccessful) {
                    Log.d(TAG, "Entrada marcada correctamente")
                    refreshAsistenciaEstado()
                    refreshIndicadores()
                } else {
                    Log.e(TAG, "Error marcando entrada: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción marcando entrada", e)
            }
        }
    }

    fun marcarSalida() {
        viewModelScope.launch {
            try {
                val id = cachedUsuarioId ?: _state.value.usuarioId
                if (id == null) {
                    resolveUsuarioId()
                    return@launch
                }
                val body = RegistrarSalidaRequest(id_usuario = id)
                val resp = api.registrarSalida(body)
                if (resp.isSuccessful) {
                    Log.d(TAG, "Salida marcada correctamente")
                    refreshAsistenciaEstado()
                    refreshIndicadores()
                } else {
                    Log.e(TAG, "Error marcando salida: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción marcando salida", e)
            }
        }
    }

    private fun refreshIndicadores() {
        viewModelScope.launch {
            try {
                val id = cachedUsuarioId ?: _state.value.usuarioId ?: return@launch
                val resp = api.getIndicadores(id)
                if (resp.isSuccessful) {
                    val dto = resp.body()
                    if (dto != null) {
                        _state.value = _state.value.copy(
                            bienestarPromedio = dto.bienestar_promedio_pct,
                            saludEmocionalProm7 = dto.salud_emocional_prom7_pct,
                            asistenciaSemanalPct = dto.asistencia_semanal_pct,
                            motivacionGeneral = dto.motivacion_general_pct
                        )
                        _uiState.value = HomeUiState.Ready
                    }
                } else {
                    Log.w(TAG, "Fallo obteniendo indicadores: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo indicadores", e)
            }
        }
    }

    companion object Factory : ViewModelProvider.Factory {
        private const val TAG = "HomeViewModel"

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel() as T
        }
    }
}