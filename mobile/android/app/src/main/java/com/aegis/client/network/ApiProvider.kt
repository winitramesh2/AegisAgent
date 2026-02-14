package com.aegis.client.network

import com.aegis.client.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

object ApiProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: AegisApi = retrofit.create(AegisApi::class.java)
}
