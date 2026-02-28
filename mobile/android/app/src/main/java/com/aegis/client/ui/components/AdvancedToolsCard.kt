package com.aegis.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aegis.client.network.ComponentStatusResponse
import com.aegis.client.network.IncidentTimelineResponse
import com.aegis.client.network.LogAnalysisResponse
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun AdvancedToolsCard(
    onUploadLog: () -> Unit,
    uploadEnabled: Boolean,
    onLoadTimeline: () -> Unit,
    timelineEnabled: Boolean,
    logAnalysis: LogAnalysisResponse?,
    timeline: IncidentTimelineResponse?,
    componentStatus: ComponentStatusResponse?,
    onRefreshComponents: () -> Unit,
    isRefreshingComponents: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Advanced Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                    Text(if (expanded) "Hide" else "Show", modifier = Modifier.padding(start = 6.dp))
                }
            }

            if (!expanded) {
                Text(
                    "Upload logs, review timeline, and inspect server health when needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onUploadLog, enabled = uploadEnabled, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                    Text("Upload Log", modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(onClick = onLoadTimeline, enabled = timelineEnabled, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.History, contentDescription = null)
                    Text("Timeline", modifier = Modifier.padding(start = 6.dp))
                }
            }

            logAnalysis?.let { analysis ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Log Analysis", fontWeight = FontWeight.SemiBold)
                        Text("Root Cause: ${analysis.rootCause}")
                        Text("Fix: ${analysis.fixAction}")
                        Text("Severity: ${analysis.severity} (${analysis.confidence})")
                    }
                }
            }

            timeline?.let { events ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Timeline Events: ${events.total}", fontWeight = FontWeight.SemiBold)
                        events.events.take(5).forEach { event ->
                            val eventType = event.readString("eventType", "UNKNOWN")
                            val timestamp = event.readString("timestamp", "-")
                            Text("- $eventType @ $timestamp")
                        }
                    }
                }
            }

            SystemHealthCard(
                componentStatus = componentStatus,
                isRefreshing = isRefreshingComponents,
                onRefresh = onRefreshComponents
            )
        }
    }
}

private fun Map<String, JsonElement>.readString(key: String, default: String): String {
    return runCatching { get(key)?.jsonPrimitive?.content ?: default }.getOrDefault(default)
}
