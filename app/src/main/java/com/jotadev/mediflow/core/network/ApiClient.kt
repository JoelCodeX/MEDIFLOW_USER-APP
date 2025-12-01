package com.jotadev.mediflow.core.network

import com.jotadev.mediflow.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

object ApiClient {
    private val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private fun selectBaseUrl(): String {
        // Detecta si corre en emulador
        val runningOnEmulator = Build.FINGERPRINT.contains("generic") ||
                Build.FINGERPRINT.lowercase().contains("emulator") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for") ||
                (Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains("ranchu"))
        return if (runningOnEmulator) BuildConfig.BASE_URL_EMULATOR else BuildConfig.BASE_URL_DEVICE
    }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(selectBaseUrl())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttp)
        .build()
}