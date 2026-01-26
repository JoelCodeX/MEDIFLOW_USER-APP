package com.jotadev.mediflow.screens.recursos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jotadev.mediflow.core.network.ApiClient
import com.jotadev.mediflow.core.network.ApiService
import com.jotadev.mediflow.core.network.ContenidoDto
import com.jotadev.mediflow.core.network.InteraccionContenidoDto
import com.jotadev.mediflow.core.network.RegistrarInteraccionContenidoRequest
import com.jotadev.mediflow.core.network.UsuarioDto
import com.jotadev.mediflow.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface RecursosUiState {
    data object Idle : RecursosUiState
    data object Loading : RecursosUiState
    data object Ready : RecursosUiState
    data class Error(val message: String) : RecursosUiState
}

data class RecursoUi(
    val id: Int,
    val title: String,
    val desc: String?,
    val tipo: String?,
    val url: String?,
    val completed: Boolean,
    val progress: Float = 0f
)

class RecursosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RecursosUiState>(RecursosUiState.Idle)
    val uiState: StateFlow<RecursosUiState> = _uiState

    private val _items = MutableStateFlow<List<RecursoUi>>(emptyList())
    val items: StateFlow<List<RecursoUi>> = _items

    private val api: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private val authRepository = AppModule.authRepository

    fun loadRecursos() {
        viewModelScope.launch {
            _uiState.value = RecursosUiState.Loading
            try {
                val usuarioId = resolveUsuarioId()
                if (usuarioId == null) {
                    _uiState.value = RecursosUiState.Error("No se pudo obtener la información del usuario")
                    return@launch
                }

                val contenidosResp = api.listarContenidos(page = 1, pageSize = 50)
                if (!contenidosResp.isSuccessful) {
                    _uiState.value = RecursosUiState.Error("Error obteniendo contenidos: ${contenidosResp.code()}")
                    return@launch
                }
                val contenidos: List<ContenidoDto> = contenidosResp.body()?.items ?: emptyList()

                val interaccionesResp = api.getInteraccionesUsuarioContenido(usuarioId)
                val interacciones: List<InteraccionContenidoDto> =
                    if (interaccionesResp.isSuccessful) {
                        interaccionesResp.body()?.items ?: emptyList()
                    } else {
                        emptyList()
                    }

                val completados = interacciones
                    .filter { it.tipo_interaccion == "completado" }
                    .mapNotNull { it.id_contenido }
                    .toSet()

                val mapped = contenidos.map { c ->
                    RecursoUi(
                        id = c.id,
                        title = c.titulo,
                        desc = c.descripcion,
                        tipo = c.tipo,
                        url = c.url_archivo,
                        completed = completados.contains(c.id)
                    )
                }
                _items.value = mapped
                _uiState.value = RecursosUiState.Ready
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando recursos", e)
                _items.value = emptyList()
                _uiState.value = RecursosUiState.Error(e.message ?: "Error cargando recursos")
            }
        }
    }

    private suspend fun resolveUsuarioId(): Int? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        try {
            val resp = api.getUsuarioByUid(uid)
            if (resp.isSuccessful) {
                val u: UsuarioDto? = resp.body()
                return u?.id
            }
            val currentUser = FirebaseAuth.getInstance().currentUser
            val idToken = try {
                currentUser?.getIdToken(true)?.await()?.token
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo idToken para recursos", e)
                null
            }
            if (idToken.isNullOrBlank()) {
                return null
            }
            val synced = try {
                authRepository.syncFirebaseToken(idToken)
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando token para recursos", e)
                false
            }
            if (!synced) {
                return null
            }
            val retry = api.getUsuarioByUid(uid)
            if (retry.isSuccessful) {
                val u2: UsuarioDto? = retry.body()
                return u2?.id
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolviendo usuarioId para recursos", e)
        }
        return null
    }

    fun registrarInteraccionVista(idContenido: Int) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val respUser = api.getUsuarioByUid(uid)
                if (!respUser.isSuccessful) {
                    return@launch
                }
                val usuario: UsuarioDto? = respUser.body()
                val usuarioId = usuario?.id ?: return@launch
                val body = RegistrarInteraccionContenidoRequest(
                    id_usuario = usuarioId,
                    id_contenido = idContenido,
                    tipo_interaccion = "visto"
                )
                api.registrarInteraccionContenido(body)
            } catch (e: Exception) {
                Log.e(TAG, "Error registrando interacción de contenido", e)
            }
        }
    }

    companion object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RecursosViewModel() as T
        }

        private const val TAG = "RecursosViewModel"
    }
}

