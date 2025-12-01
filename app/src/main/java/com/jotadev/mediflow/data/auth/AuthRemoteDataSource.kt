package com.jotadev.mediflow.data.auth

import com.jotadev.mediflow.core.network.ApiClient
import com.jotadev.mediflow.core.network.ApiService
import com.jotadev.mediflow.core.network.FirebaseTokenRequest

class AuthRemoteDataSource(
    private val api: ApiService = ApiClient.retrofit.create(ApiService::class.java)
) {
    suspend fun syncFirebaseToken(idToken: String): Boolean {
        val response = api.syncFirebase(FirebaseTokenRequest(id_token = idToken))
        return response.isSuccessful
    }
}