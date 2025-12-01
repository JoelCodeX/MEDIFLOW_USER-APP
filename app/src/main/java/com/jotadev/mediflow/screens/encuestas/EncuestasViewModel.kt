package com.jotadev.mediflow.screens.encuestas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.mediflow.core.network.ApiClient
import com.jotadev.mediflow.core.network.ApiService
import com.jotadev.mediflow.core.network.EncuestaDto
import com.jotadev.mediflow.core.network.ResponderEncuestaRequest
import com.jotadev.mediflow.core.network.RespuestaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EncuestasState(
    val pending: List<EncuestaDto> = emptyList(),
    val current: EncuestaDto? = null,
    val answers: Map<Int, String> = emptyMap(),
    val loading: Boolean = false,
    val error: String? = null
)

class EncuestasViewModel : ViewModel() {
    private val api: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private val _state = MutableStateFlow(EncuestasState())
    val state: StateFlow<EncuestasState> = _state

    fun loadPending(usuarioId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val resp = api.getEncuestasPendientes(usuarioId)
                val list = resp.body() ?: emptyList()
                _state.value = _state.value.copy(pending = list)
                if (list.isNotEmpty()) {
                    ensureDetails(list.first().id)
                } else {
                    _state.value = _state.value.copy(current = null)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, loading = false)
            } finally {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    private suspend fun ensureDetails(encuestaId: Int) {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            val resp = api.getEncuesta(encuestaId)
            val detail = resp.body()
            _state.value = _state.value.copy(current = detail, answers = emptyMap())
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message)
        } finally {
            _state.value = _state.value.copy(loading = false)
        }
    }

    fun setAnswer(preguntaId: Int, value: String) {
        _state.value = _state.value.copy(
            answers = _state.value.answers.toMutableMap().apply { put(preguntaId, value) }
        )
    }

    fun submit(usuarioId: Int, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = _state.value.current ?: return@launch
            val items = _state.value.answers.map { (pid, ans) ->
                RespuestaItem(pregunta_id = pid, respuesta = ans)
            }
            try {
                val resp = api.responderEncuesta(
                    current.id,
                    ResponderEncuestaRequest(id_usuario = usuarioId, respuestas = items)
                )
                val ok = resp.isSuccessful
                if (ok) {
                    val remaining = _state.value.pending.drop(1)
                    _state.value = _state.value.copy(pending = remaining)
                    if (remaining.isNotEmpty()) {
                        ensureDetails(remaining.first().id)
                    } else {
                        _state.value = _state.value.copy(current = null)
                    }
                }
                onDone(ok)
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }
}