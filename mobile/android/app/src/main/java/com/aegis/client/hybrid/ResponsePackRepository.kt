package com.aegis.client.hybrid

import android.content.Context
import kotlinx.serialization.json.Json

class ResponsePackRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private var cached: ResponsePack? = null

    fun loadPack(): ResponsePack? {
        cached?.let { return it }
        return runCatching {
            context.assets.open("response_pack.json").bufferedReader().use { reader ->
                json.decodeFromString<ResponsePack>(reader.readText())
            }
        }.getOrNull()?.also { cached = it }
    }
}
