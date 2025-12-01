package com.jotadev.mediflow.domain.auth

interface AuthRepository {
    suspend fun syncFirebaseToken(idToken: String): Boolean
}