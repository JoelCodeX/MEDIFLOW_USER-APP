package com.jotadev.mediflow.di

import com.jotadev.mediflow.data.auth.AuthRemoteDataSource
import com.jotadev.mediflow.data.auth.AuthRepositoryImpl
import com.jotadev.mediflow.domain.auth.AuthRepository

object AppModule {
    private val remoteDataSource: AuthRemoteDataSource by lazy { AuthRemoteDataSource() }
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(remoteDataSource) }
}