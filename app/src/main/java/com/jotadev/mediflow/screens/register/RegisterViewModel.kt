package com.jotadev.mediflow.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.jotadev.mediflow.domain.auth.AuthRepository
import com.jotadev.mediflow.di.AppModule
import androidx.lifecycle.viewModelScope
import com.jotadev.mediflow.core.network.ApiClient
import com.jotadev.mediflow.core.network.ApiService
import com.jotadev.mediflow.core.network.SyncFirebaseUserRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        rol: String?,
        dni: String?,
        area: String?,
        cargo: String?
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                // Crear usuario con Firebase
                val result = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    // Actualizar el displayName del usuario creado
                    try {
                        val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profileUpdate).await()
                    } catch (_: Exception) { /* Ignorar errores de perfil */ }

                    // Sincronizar datos adicionales (dni, area, cargo, rol) en backend
                    try {
                        val api: ApiService = ApiClient.retrofit.create(ApiService::class.java)
                        val body = SyncFirebaseUserRequest(
                            uid_firebase = user.uid,
                            nombre = name.split(" ").firstOrNull(),
                            apellido = name.split(" ").drop(1).joinToString(" ").ifEmpty { null },
                            rol = rol,
                            dni = dni,
                            area = area,
                            cargo = cargo
                        )
                        api.syncFirebaseUser(body)
                    } catch (e: Exception) { 
                        _uiState.value = RegisterUiState.Error("Error al sincronizar datos adicionales: ${e.message}")
                        return@launch
                    }

                    // Obtener ID token de Firebase y sincronizar con backend
                    try {
                        val firebaseIdToken = user.getIdToken(true).await()?.token
                        if (firebaseIdToken != null) {
                            val synced = authRepository.syncFirebaseToken(firebaseIdToken)
                            if (synced) {
                                _uiState.value = RegisterUiState.Success
                            } else {
                                _uiState.value = RegisterUiState.Error("Error al autenticar con el backend. Verifica tu conexión.")
                            }
                        } else {
                            _uiState.value = RegisterUiState.Error("No se pudo obtener el token de autenticación de Firebase")
                        }
                    } catch (e: Exception) {
                        _uiState.value = RegisterUiState.Error("Error en la autenticación: ${e.message}")
                    }
                } else {
                    _uiState.value = RegisterUiState.Error("Error al crear usuario")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    companion object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(AppModule.authRepository) as T
        }
    }
}