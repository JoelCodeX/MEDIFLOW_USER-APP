package com.jotadev.mediflow.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.jotadev.mediflow.domain.auth.AuthRepository
import com.jotadev.mediflow.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun loginWithGoogle(idToken: String) {
        _uiState.value = LoginUiState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Tras autenticar con Google, obtener el ID token de Firebase y sincronizar con backend
                    viewModelScope.launch {
                        try {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            val firebaseIdToken = firebaseUser?.getIdToken(true)?.await()?.token
                            if (firebaseIdToken != null) {
                                val synced = authRepository.syncFirebaseToken(firebaseIdToken)
                                if (synced) {
                                    _uiState.value = LoginUiState.Success
                                } else {
                                    _uiState.value = LoginUiState.Error("No se pudo sincronizar el usuario en el backend")
                                }
                            } else {
                                _uiState.value = LoginUiState.Error("No se obtuvo token de Firebase")
                            }
                        } catch (e: Exception) {
                            _uiState.value = LoginUiState.Error(e.message ?: "Error al sincronizar con backend")
                        }
                    }
                } else {
                    _uiState.value = LoginUiState.Error(task.exception?.message ?: "Login con Google fallido")
                }
            }
    }

    fun loginWithEmail(email: String, password: String) {
        _uiState.value = LoginUiState.Loading
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Tras autenticar con Email/Password, obtener el ID token de Firebase y sincronizar con backend
                    viewModelScope.launch {
                        try {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            val firebaseIdToken = firebaseUser?.getIdToken(true)?.await()?.token
                            if (firebaseIdToken != null) {
                                val synced = authRepository.syncFirebaseToken(firebaseIdToken)
                                if (synced) {
                                    _uiState.value = LoginUiState.Success
                                } else {
                                    _uiState.value = LoginUiState.Error("No se pudo sincronizar el usuario en el backend")
                                }
                            } else {
                                _uiState.value = LoginUiState.Error("No se obtuvo token de Firebase")
                            }
                        } catch (e: Exception) {
                            _uiState.value = LoginUiState.Error(e.message ?: "Error al sincronizar con backend")
                        }
                    }
                } else {
                    _uiState.value = LoginUiState.Error(task.exception?.message ?: "Login fallido")
                }
            }
    }

    companion object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(AppModule.authRepository) as T
        }
    }
}