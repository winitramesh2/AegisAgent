package com.aegis.client.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val query: String,
    val platform: String,
    @SerialName("userId") val userId: String,
    @SerialName("deviceMetadata") val deviceMetadata: Map<String, String>,
    @SerialName("troubleshootingFailed") val troubleshootingFailed: Boolean = false,
    @SerialName("correlationId") val correlationId: String? = null,
    @SerialName("authProtocol") val authProtocol: String? = null
)

@Serializable
data class ChatResponse(
    val intent: String,
    val confidence: Double,
    val message: String,
    val actions: List<String> = emptyList(),
    @SerialName("escalationTicketId") val escalationTicketId: String? = null,
    val status: String,
    @SerialName("correlationId") val correlationId: String
)

@Serializable
data class LogAnalysisResponse(
    @SerialName("rootCause") val rootCause: String,
    @SerialName("fixAction") val fixAction: String,
    val severity: String,
    val confidence: Double,
    @SerialName("matchedSignals") val matchedSignals: List<String> = emptyList(),
    @SerialName("correlationId") val correlationId: String
)

@Serializable
data class IncidentTimelineResponse(
    @SerialName("correlationId") val correlationId: String? = null,
    val total: Int = 0,
    val events: List<Map<String, String>> = emptyList()
)
