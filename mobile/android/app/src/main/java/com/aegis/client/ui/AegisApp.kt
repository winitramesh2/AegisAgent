package com.aegis.client.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AegisApp(viewModel: AegisViewModel = viewModel()) {
    val state by viewModel.ui.collectAsState()
    val context = LocalContext.current

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

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Describe issue") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.submitChat() }) { Text("Send Chat") }
            Button(onClick = { filePicker.launch(arrayOf("text/*", "*/*")) }) { Text("Upload Log") }
            Button(onClick = { viewModel.fetchTimeline() }) { Text("Fetch Timeline") }
        }

        if (state.loading) {
            CircularProgressIndicator()
        }

        state.error?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }

        state.chat?.let { chat ->
            Text("Chat Status: ${chat.status}", fontWeight = FontWeight.SemiBold)
            Text("Intent: ${chat.intent} (${chat.confidence})")
            Text("Message: ${chat.message}")
            if (chat.actions.isNotEmpty()) {
                Text("Actions:")
                chat.actions.forEach { Text("- $it") }
            }
            chat.escalationTicketId?.let { Text("Ticket: $it") }
        }

        state.logAnalysis?.let { analysis ->
            Text("Log Analysis", fontWeight = FontWeight.SemiBold)
            Text("Root Cause: ${analysis.rootCause}")
            Text("Fix: ${analysis.fixAction}")
            Text("Severity: ${analysis.severity} (${analysis.confidence})")
        }

        state.timeline?.let { timeline ->
            Text("Timeline Events: ${timeline.total}", fontWeight = FontWeight.SemiBold)
            timeline.events.take(5).forEach { event ->
                val eventType = event["eventType"] ?: "UNKNOWN"
                val timestamp = event["timestamp"] ?: "-"
                Text("- $eventType @ $timestamp")
            }
        }
    }
}
