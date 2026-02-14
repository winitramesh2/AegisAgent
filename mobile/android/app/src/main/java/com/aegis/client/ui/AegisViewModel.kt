package com.aegis.client.ui

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.client.network.AegisApi
import com.aegis.client.network.ApiProvider
import com.aegis.client.network.ChatRequest
import com.aegis.client.network.ChatResponse
import com.aegis.client.network.IncidentTimelineResponse
import com.aegis.client.network.LogAnalysisResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

data class AegisUiState(
    val query: String = "",
    val chat: ChatResponse? = null,
    val logAnalysis: LogAnalysisResponse? = null,
    val timeline: IncidentTimelineResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val correlationId: String = UUID.randomUUID().toString()
)

class AegisViewModel(
    private val api: AegisApi = ApiProvider.api
) : ViewModel() {

    private val _ui = MutableStateFlow(AegisUiState())
    val ui: StateFlow<AegisUiState> = _ui.asStateFlow()

    fun onQueryChange(query: String) {
        _ui.value = _ui.value.copy(query = query)
    }

    fun submitChat() {
        val current = _ui.value
        if (current.query.isBlank()) return

        viewModelScope.launch {
            _ui.value = current.copy(loading = true, error = null)
            runCatching {
                api.chat(
                    ChatRequest(
                        query = current.query,
                        platform = "Android",
                        userId = "demo-user",
                        authProtocol = "totp",
                        correlationId = current.correlationId,
                        deviceMetadata = mapOf(
                            "model" to Build.MODEL,
                            "sdkInt" to Build.VERSION.SDK_INT.toString(),
                            "manufacturer" to Build.MANUFACTURER
                        )
                    )
                )
            }.onSuccess { chat ->
                _ui.value = _ui.value.copy(loading = false, chat = chat, correlationId = chat.correlationId)
            }.onFailure { ex ->
                _ui.value = _ui.value.copy(loading = false, error = ex.message ?: "Chat request failed")
            }
        }
    }

    fun uploadLog(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            runCatching {
                val bytes = resolver.openInputStream(uri)?.readBytes() ?: error("Unable to read file")
                val body = bytes.toRequestBody("text/plain".toMediaType())
                val part = MultipartBody.Part.createFormData("logFile", "client.log", body)
                val correlationBody: RequestBody = _ui.value.correlationId.toRequestBody("text/plain".toMediaType())
                api.analyzeLogs(part, correlationBody)
            }.onSuccess { analysis ->
                _ui.value = _ui.value.copy(loading = false, logAnalysis = analysis)
            }.onFailure { ex ->
                _ui.value = _ui.value.copy(loading = false, error = ex.message ?: "Log analysis failed")
            }
        }
    }

    fun fetchTimeline() {
        viewModelScope.launch {
            val correlationId = _ui.value.correlationId
            _ui.value = _ui.value.copy(loading = true, error = null)
            runCatching { api.incidentTimeline(correlationId) }
                .onSuccess { timeline ->
                    _ui.value = _ui.value.copy(loading = false, timeline = timeline)
                }
                .onFailure { ex ->
                    _ui.value = _ui.value.copy(loading = false, error = ex.message ?: "Timeline fetch failed")
                }
        }
    }
}
