package com.aegis.client.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun AegisApp(viewModel: AegisViewModel = viewModel()) {
    val state by viewModel.ui.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.loadComponentStatus()
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadLog(context.contentResolver, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aegis Android Client", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Correlation: ${state.correlationId}", style = MaterialTheme.typography.bodySmall)

        state.componentStatus?.let { components ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Server Components", fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        components.statuses.toSortedMap().forEach { (name, status) ->
                            StatusChip(name = name, status = status)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Describe issue") },
            minLines = 4,
            maxLines = 6,
            placeholder = { Text("Example: OTP code not generating on Android") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.submitChat() },
                enabled = state.query.isNotBlank() && !state.loading
            ) { Text("Send Chat") }
            OutlinedButton(onClick = { filePicker.launch(arrayOf("text/*", "*/*")) }, enabled = !state.loading) { Text("Upload Log") }
            OutlinedButton(onClick = { viewModel.fetchTimeline() }, enabled = !state.loading) { Text("Fetch Timeline") }
            OutlinedButton(onClick = { viewModel.loadComponentStatus() }, enabled = !state.loading) { Text("Refresh Status") }
        }

        if (state.loading) {
            CircularProgressIndicator()
        }

        state.error?.let {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        state.chat?.let { chat ->
            val statusText = buildString {
                appendLine("Chat Status: ${chat.status}")
                appendLine("Intent: ${chat.intent} (${chat.confidence})")
                appendLine("Message: ${chat.message}")
                if (chat.actions.isNotEmpty()) {
                    appendLine("Actions:")
                    chat.actions.forEach { appendLine("- $it") }
                }
                if (chat.escalationTicketId != null) {
                    appendLine("Ticket: ${chat.escalationTicketId}")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectionContainer {
                        Text(statusText)
                    }
                    OutlinedButton(onClick = { clipboardManager.setText(AnnotatedString(statusText)) }) {
                        Text("Copy Status")
                    }
                }
            }
        }

        state.logAnalysis?.let { analysis ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Log Analysis", fontWeight = FontWeight.SemiBold)
                    Text("Root Cause: ${analysis.rootCause}")
                    Text("Fix: ${analysis.fixAction}")
                    Text("Severity: ${analysis.severity} (${analysis.confidence})")
                }
            }
        }

        state.timeline?.let { timeline ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Timeline Events: ${timeline.total}", fontWeight = FontWeight.SemiBold)
                    timeline.events.take(5).forEach { event ->
                        val eventType = event.readString("eventType", "UNKNOWN")
                        val timestamp = event.readString("timestamp", "-")
                        Text("- $eventType @ $timestamp")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(name: String, status: String) {
    val isUp = status.equals("UP", ignoreCase = true)
    val color = if (isUp) Color(0xFF2E7D32) else Color(0xFFC62828)
    val background = if (isUp) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Row(
        modifier = Modifier
            .background(background, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, shape = MaterialTheme.shapes.small))
        Text("${name.uppercase()}: $status", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

private fun Map<String, JsonElement>.readString(key: String, default: String): String {
    return runCatching { get(key)?.jsonPrimitive?.content ?: default }.getOrDefault(default)
}
