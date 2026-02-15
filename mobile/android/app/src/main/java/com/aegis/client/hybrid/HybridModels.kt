package com.aegis.client.hybrid

import kotlinx.serialization.Serializable

@Serializable
data class ResponsePack(
    val version: String,
    val intents: List<ResponseIntent>
)

@Serializable
data class ResponseIntent(
    val intent: String,
    val diagnosis: String,
    val actions: List<String> = emptyList()
)

data class LocalIntentPrediction(
    val intent: String,
    val confidence: Double
)

data class HybridDiagnosis(
    val intent: String,
    val confidence: Double,
    val diagnosis: String,
    val actions: List<String>,
    val source: String
)
