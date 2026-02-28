package com.aegis.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.client.ui.components.ActionChecklistCard
import com.aegis.client.ui.components.AdvancedToolsCard
import com.aegis.client.ui.components.DecisionBar
import com.aegis.client.ui.components.DiagnosisCard
import com.aegis.client.ui.components.EscalationStatusCard
import com.aegis.client.ui.components.IncidentInputCard
import com.aegis.client.ui.components.InlineStatusMessage
import com.aegis.client.ui.components.ModeInfoCard

@Composable
fun HybridAiWorkflowScreen(
    state: AegisUiState,
    onQueryChange: (String) -> Unit,
    onRunOnDevice: () -> Unit,
    onRunOnCloud: () -> Unit,
    onRetry: () -> Unit,
    onEscalate: () -> Unit,
    onMarkResolved: () -> Unit,
    onToggleAction: (Int) -> Unit,
    onUploadLog: () -> Unit,
    onLoadTimeline: () -> Unit,
    onRefreshComponents: () -> Unit,
    onCopyStatus: (String) -> Unit
) {
    val hasTriedAnyAction = state.actionChecklist.any { it.done }
    val canRetry = state.chat != null && hasTriedAnyAction && !state.isRetrying && !state.isDiagnosing && state.stage != WorkflowStage.Resolved && state.stage != WorkflowStage.Escalated
    val canEscalate = state.chat != null && (state.retryCount > 0 || state.stage == WorkflowStage.RetryResult) && !state.isEscalating && state.stage != WorkflowStage.Resolved && state.stage != WorkflowStage.Escalated
    val canResolve = state.chat != null && state.stage != WorkflowStage.Resolved && state.stage != WorkflowStage.Escalated

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IncidentInputCard(
                title = "Describe Your Issue",
                query = state.query,
                onQueryChange = onQueryChange,
                placeholder = "Example: Push approval timeout on Android",
                helperText = "Hybrid mode starts on-device for speed; use Run On-Cloud for deeper online analysis.",
                primaryLabel = "Run On-Device",
                primaryIcon = Icons.Filled.Psychology,
                onPrimaryClick = onRunOnDevice,
                primaryEnabled = state.query.isNotBlank() && !state.hybridLoading,
                secondaryLabel = "Run On-Cloud",
                secondaryIcon = Icons.AutoMirrored.Filled.Send,
                onSecondaryClick = onRunOnCloud,
                secondaryEnabled = state.query.isNotBlank() && !state.isBusy
            )
        }

        item {
            ModeInfoCard(
                title = "How Hybrid Mode works",
                description = "On-device model gives quick guidance even with unstable network. Run On-Cloud when you need deeper diagnosis."
            )
        }

        if (state.hybridLoading) {
            item {
                Card {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Text("Running on-device diagnosis...")
                    }
                }
            }
        }

        state.hybridError?.let { message ->
            item { InlineStatusMessage(message = message, isError = true) }
        }

        state.hybridResult?.let { result ->
            item {
                DiagnosisCard(
                    title = "On-Device Analysis",
                    intent = result.intent,
                    confidence = result.confidence,
                    message = result.diagnosis
                )
            }
        }

        state.chat?.let { chat ->
            item {
                val statusText = "Status: ${chat.status}\nDetected issue category: ${chat.intent}\nConfidence score: ${chat.confidence}"
                DiagnosisCard(
                    title = "Cloud Analysis",
                    intent = chat.intent,
                    confidence = chat.confidence,
                    message = chat.message,
                    onCopyClick = { onCopyStatus(statusText) }
                )
            }
        }

        if (state.error != null) {
            item { InlineStatusMessage(message = state.error, isError = true) }
        }

        if (state.actionChecklist.isNotEmpty() && state.stage != WorkflowStage.Resolved && state.stage != WorkflowStage.Escalated) {
            item { ActionChecklistCard(actions = state.actionChecklist, onToggleAction = onToggleAction) }
        }

        item {
            DecisionBar(
                showResolve = canResolve,
                resolveEnabled = canResolve,
                onResolve = onMarkResolved,
                showRetry = state.chat != null,
                retryEnabled = canRetry,
                onRetry = onRetry,
                showEscalate = state.chat != null,
                escalateEnabled = canEscalate,
                onEscalate = onEscalate
            )
        }

        if (state.stage == WorkflowStage.Escalated || state.chat?.status.equals("ESCALATED", ignoreCase = true)) {
            val chat = state.chat
            if (chat != null) {
                item { EscalationStatusCard(ticketId = chat.escalationTicketId, message = chat.message) }
            }
        }

        if (state.stage == WorkflowStage.Resolved) {
            item {
                InlineStatusMessage(
                    message = "Resolved. You can still use cloud follow-up if needed.",
                    isError = false
                )
            }
        }

        item {
            AdvancedToolsCard(
                onUploadLog = onUploadLog,
                uploadEnabled = !state.isUploading && !state.isBusy,
                onLoadTimeline = onLoadTimeline,
                timelineEnabled = !state.isLoadingTimeline && !state.isBusy,
                logAnalysis = state.logAnalysis,
                timeline = state.timeline,
                componentStatus = state.componentStatus,
                onRefreshComponents = onRefreshComponents,
                isRefreshingComponents = state.isLoadingComponents
            )
        }
    }
}
