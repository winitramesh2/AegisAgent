package com.aegis.client.ui

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.client.hybrid.HybridAiEngine
import com.aegis.client.hybrid.HybridDiagnosis
import com.aegis.client.hybrid.ResponsePackRepository
import com.aegis.client.hybrid.RuleBasedLocalIntentClassifier
import com.aegis.client.network.AegisApi
import com.aegis.client.network.ApiProvider
import com.aegis.client.network.ChatRequest
import com.aegis.client.network.ChatResponse
import com.aegis.client.network.ComponentStatusResponse
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
    val componentStatus: ComponentStatusResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val hybridResult: HybridDiagnosis? = null,
    val hybridLoading: Boolean = false,
    val hybridError: String? = null,
    val retryCount: Int = 0,
    val correlationId: String = UUID.randomUUID().toString()
)

class AegisViewModel(
    private val api: AegisApi = ApiProvider.api
) : ViewModel() {

    private val _ui = MutableStateFlow(AegisUiState())
    val ui: StateFlow<AegisUiState> = _ui.asStateFlow()

    private var hybridEngine: HybridAiEngine? = null

    fun onQueryChange(query: String) {
        _ui.value = _ui.value.copy(query = query, error = null, hybridError = null, retryCount = 0)
    }

    fun loadComponentStatus() {
        viewModelScope.launch {
            runCatching { api.componentStatus() }
                .onSuccess { status ->
                    _ui.value = _ui.value.copy(componentStatus = status)
                }
                .onFailure { ex ->
                    _ui.value = _ui.value.copy(error = ex.message ?: "Component status check failed")
                }
        }
    }

    fun submitChat() {
        val current = _ui.value
        if (current.query.isBlank()) {
            _ui.value = current.copy(error = "Please describe the issue before sending chat")
            return
        }

        viewModelScope.launch {
            _ui.value = current.copy(loading = true, error = null)
            runCatching {
                api.chat(buildRequest(query = current.query, correlationId = current.correlationId, retryAttempt = false))
            }.onSuccess { chat ->
                _ui.value = _ui.value.copy(loading = false, chat = chat, correlationId = chat.correlationId, retryCount = 0)
            }.onFailure { ex ->
                _ui.value = _ui.value.copy(loading = false, error = ex.message ?: "Chat request failed")
            }
        }
    }

    fun retryChat() {
        val current = _ui.value
        val previous = current.chat
        if (current.query.isBlank()) {
            _ui.value = current.copy(error = "Please describe the issue before retry")
            return
        }
        if (previous == null) {
            _ui.value = current.copy(error = "Run Send Chat first to generate initial resolution")
            return
        }

        val nextAttemptCount = current.retryCount + 1
        viewModelScope.launch {
            _ui.value = current.copy(loading = true, error = null)
            runCatching {
                api.chat(
                    buildRequest(
                        query = current.query,
                        correlationId = current.correlationId,
                        retryAttempt = true,
                        previousDiagnosis = previous.message,
                        attemptedActions = previous.actions,
                        attemptCount = nextAttemptCount
                    )
                )
            }.onSuccess { chat ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    chat = chat,
                    correlationId = chat.correlationId,
                    retryCount = nextAttemptCount
                )
            }.onFailure { ex ->
                _ui.value = _ui.value.copy(loading = false, error = ex.message ?: "Retry request failed")
            }
        }
    }

    fun escalateIssue() {
        val current = _ui.value
        if (current.query.isBlank()) {
            _ui.value = current.copy(error = "Please describe the issue before escalation")
            return
        }

        viewModelScope.launch {
            _ui.value = current.copy(loading = true, error = null)
            runCatching {
                api.escalate(buildRequest(query = current.query, correlationId = current.correlationId, troubleshootingFailed = true))
            }.onSuccess { chat ->
                _ui.value = _ui.value.copy(loading = false, chat = chat, correlationId = chat.correlationId)
            }.onFailure { ex ->
                _ui.value = _ui.value.copy(loading = false, error = ex.message ?: "Escalation failed")
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

    fun runHybridDiagnosis(context: android.content.Context) {
        val current = _ui.value
        if (current.query.isBlank()) {
            _ui.value = current.copy(hybridError = "Please describe the issue before running on-device AI")
            return
        }

        viewModelScope.launch {
            _ui.value = current.copy(hybridLoading = true, hybridError = null)
            runCatching {
                val engine = hybridEngine ?: HybridAiEngine(
                    classifier = RuleBasedLocalIntentClassifier(),
                    repository = ResponsePackRepository(context.applicationContext)
                ).also { hybridEngine = it }
                engine.diagnose(current.query)
            }.onSuccess { result ->
                if (result == null) {
                    _ui.value = _ui.value.copy(
                        hybridLoading = false,
                        hybridResult = null,
                        hybridError = "On-device confidence was below the threshold. Use Core AI for cloud fallback."
                    )
                } else {
                    _ui.value = _ui.value.copy(hybridLoading = false, hybridResult = result)
                }
            }.onFailure { ex ->
                _ui.value = _ui.value.copy(
                    hybridLoading = false,
                    hybridError = ex.message ?: "Hybrid AI failed to run"
                )
            }
        }
    }

    private fun buildRequest(
        query: String,
        correlationId: String,
        retryAttempt: Boolean = false,
        troubleshootingFailed: Boolean = false,
        previousDiagnosis: String? = null,
        attemptedActions: List<String>? = null,
        attemptCount: Int? = null
    ): ChatRequest {
        return ChatRequest(
            query = query,
            platform = "Android",
            userId = "demo-user",
            authProtocol = "totp",
            troubleshootingFailed = troubleshootingFailed,
            retryAttempt = retryAttempt,
            previousDiagnosis = previousDiagnosis,
            attemptedActions = attemptedActions,
            attemptCount = attemptCount,
            correlationId = correlationId,
            deviceMetadata = mapOf(
                "model" to Build.MODEL,
                "sdkInt" to Build.VERSION.SDK_INT.toString(),
                "manufacturer" to Build.MANUFACTURER
            )
        )
    }
}
