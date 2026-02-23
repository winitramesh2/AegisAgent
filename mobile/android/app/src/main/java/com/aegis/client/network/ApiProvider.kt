package com.aegis.client.network

import com.aegis.client.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object ApiProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val key = BuildConfig.API_AUTH_KEY
            if (key.isBlank()) {
                return@addInterceptor chain.proceed(original)
            }
            val updated = original.newBuilder()
                .addHeader("X-API-Key", key)
                .build()
            chain.proceed(updated)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: AegisApi = retrofit.create(AegisApi::class.java)
}
