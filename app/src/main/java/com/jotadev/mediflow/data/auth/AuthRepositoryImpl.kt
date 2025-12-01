package com.jotadev.mediflow.data.auth

import com.jotadev.mediflow.domain.auth.AuthRepository

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource = AuthRemoteDataSource()
) : AuthRepository {
    override suspend fun syncFirebaseToken(idToken: String): Boolean =
        remote.syncFirebaseToken(idToken)
}