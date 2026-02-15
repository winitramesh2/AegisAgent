package com.aegis.client.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisApp(viewModel: AegisViewModel = viewModel()) {
    val state by viewModel.ui.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var chatPanelOpen by remember { mutableStateOf(true) }
    var componentDetailsExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadComponentStatus()
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadLog(context.contentResolver, uri)
        }
    }

    val componentStatus = state.componentStatus
    val anyDown = componentStatus?.components?.values?.any {
        !it.status.equals("UP", ignoreCase = true) && !it.status.equals("RUNNING", ignoreCase = true)
    } ?: false
    val statusColor = when {
        componentStatus == null -> MaterialTheme.colorScheme.onSurfaceVariant
        anyDown -> Color(0xFFC62828)
        else -> Color(0xFF2E7D32)
    }
    val statusBackground = when {
        componentStatus == null -> Color(0xFFF1F3F5)
        anyDown -> Color(0xFFFFEBEE)
        else -> Color(0xFFE8F5E9)
    }
    val statusLabel = when {
        componentStatus == null -> "Status Pending"
        anyDown -> "Offline"
        else -> "Online"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aegis Support Console", fontWeight = FontWeight.Bold)
                        Text(
                            "Client Apps • Android",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Surface(color = statusBackground, shape = RoundedCornerShape(999.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(statusColor, shape = CircleShape))
                            Text(
                                text = statusLabel,
                                color = statusColor,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { chatPanelOpen = !chatPanelOpen }) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Start chat")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Core AI") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Hybrid AI") }
                )
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    componentStatus?.let { components ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Server Components", fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(onClick = { componentDetailsExpanded = !componentDetailsExpanded }) {
                                            Icon(
                                                imageVector = if (componentDetailsExpanded) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                contentDescription = null
                                            )
                                            Text(if (componentDetailsExpanded) "Hide" else "Show", modifier = Modifier.padding(start = 6.dp))
                                        }
                                        IconButton(onClick = { viewModel.loadComponentStatus() }, enabled = !state.loading) {
                                            Icon(Icons.Filled.Sync, contentDescription = "Refresh")
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    components.components.toSortedMap().forEach { (name, item) ->
                                        MiniStatusChip(name = name, item = item)
                                    }
                                }

                                if (componentDetailsExpanded) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        components.components.toSortedMap().forEach { (name, item) ->
                                            CompactStatusRow(name = name, item = item)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!chatPanelOpen) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Text(
                                text = "Chat session minimized. Tap the floating chat button to open.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        return@Column
                    }

                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        label = { Text("Describe issue") },
                        minLines = 4,
                        maxLines = 6,
                        placeholder = { Text("Example: OTP code not generating on Android") },
                        readOnly = false,
                        singleLine = false
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.submitChat() },
                            enabled = state.query.isNotBlank() && !state.loading,
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Text("Send", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { viewModel.retryChat() },
                            enabled = state.chat != null && state.query.isNotBlank() && !state.loading,
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Text("Retry", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { viewModel.escalateIssue() },
                            enabled = state.chat != null && state.query.isNotBlank() && !state.loading,
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.ReportProblem, contentDescription = null)
                            Text("Escalate", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { filePicker.launch(arrayOf("text/*", "*/*")) },
                            enabled = !state.loading,
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null)
                            Text("Upload", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { viewModel.fetchTimeline() },
                            enabled = !state.loading,
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.History, contentDescription = null)
                            Text("Timeline", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                        }
                    }
                    if (state.loading) {
                        CircularProgressIndicator()
                    }

                    state.error?.let {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
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
                        }

                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SelectionContainer {
                                    Text(statusText)
                                }
                                IconButton(onClick = { clipboardManager.setText(AnnotatedString(statusText)) }) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy status")
                                }
                            }
                        }

                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Diagnosis", fontWeight = FontWeight.SemiBold)
                                Text(chat.message)
                                if (chat.actions.isNotEmpty()) {
                                    Text("Actions", fontWeight = FontWeight.SemiBold)
                                    chat.actions.forEach { action ->
                                        Text("- $action")
                                    }
                                }
                                if (chat.status.equals("ESCALATED", ignoreCase = true)) {
                                    Text("Please wait for 3 working days. Support team will contact you.")
                                }
                            }
                        }
                    }

                    state.logAnalysis?.let { analysis ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Log Analysis", fontWeight = FontWeight.SemiBold)
                                Text("Root Cause: ${analysis.rootCause}")
                                Text("Fix: ${analysis.fixAction}")
                                Text("Severity: ${analysis.severity} (${analysis.confidence})")
                            }
                        }
                    }

                    state.timeline?.let { timeline ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        label = { Text("Describe issue") },
                        minLines = 3,
                        maxLines = 5,
                        placeholder = { Text("Example: Push approval timeout on Android") },
                        readOnly = false,
                        singleLine = false
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.runHybridDiagnosis(context) },
                            enabled = state.query.isNotBlank() && !state.hybridLoading,
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Psychology, contentDescription = null)
                            Text("Run On-Device", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { viewModel.submitChat() },
                            enabled = state.query.isNotBlank() && !state.loading,
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Text("Core Fallback", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                        }
                    }

                    if (state.hybridLoading) {
                        CircularProgressIndicator()
                    }

                    state.hybridError?.let { error ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    state.hybridResult?.let { result ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("On-Device Result", fontWeight = FontWeight.SemiBold)
                                Text("Intent: ${result.intent}")
                                Text("Confidence: ${String.format("%.2f", result.confidence)}")
                                Text("Source: ${result.source}")
                                Text("Diagnosis", fontWeight = FontWeight.SemiBold)
                                Text(result.diagnosis)
                                if (result.actions.isNotEmpty()) {
                                    Text("Actions", fontWeight = FontWeight.SemiBold)
                                    result.actions.forEach { action ->
                                        Text("- $action")
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun CompactStatusRow(name: String, item: com.aegis.client.network.ComponentStatusItem) {
    val running = item.status.equals("UP", ignoreCase = true) || item.status.equals("RUNNING", ignoreCase = true)
    val statusLabel = if (running) "Running" else "Down"
    val color = if (running) Color(0xFF2E7D32) else Color(0xFFC62828)
    val background = if (running) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(iconForComponent(name), contentDescription = null, tint = color)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val inlineDetail = item.detail?.let { " • $it" } ?: ""
            Text("${name.uppercase()} • $statusLabel$inlineDetail", style = MaterialTheme.typography.labelMedium, color = color)
            item.url?.let {
                SelectionContainer {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item.detail?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MiniStatusChip(name: String, item: com.aegis.client.network.ComponentStatusItem) {
    val running = item.status.equals("UP", ignoreCase = true) || item.status.equals("RUNNING", ignoreCase = true)
    val statusLabel = if (running) "Running" else "Down"
    val color = if (running) Color(0xFF2E7D32) else Color(0xFFC62828)
    val background = if (running) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Surface(
        color = background,
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(iconForComponent(name), contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(
                text = "${name.uppercase().take(5)} $statusLabel",
                color = color,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

private fun iconForComponent(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (name.lowercase()) {
        "backend" -> Icons.Filled.Dns
        "deeppavlov" -> Icons.Filled.Psychology
        "opensearch" -> Icons.Filled.Storage
        "jira" -> Icons.Filled.ConfirmationNumber
        "email" -> Icons.Filled.Email
        else -> Icons.Filled.Storage
    }
}

private fun Map<String, JsonElement>.readString(key: String, default: String): String {
    return runCatching { get(key)?.jsonPrimitive?.content ?: default }.getOrDefault(default)
}
