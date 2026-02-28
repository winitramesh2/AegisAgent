package com.aegis.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DecisionBar(
    showResolve: Boolean,
    resolveEnabled: Boolean,
    onResolve: () -> Unit,
    showRetry: Boolean,
    retryEnabled: Boolean,
    onRetry: () -> Unit,
    showEscalate: Boolean,
    escalateEnabled: Boolean,
    onEscalate: () -> Unit
) {
    if (!showResolve && !showRetry && !showEscalate) {
        return
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Next Step", modifier = Modifier.padding(start = 2.dp))
        if (showResolve) {
            Button(onClick = onResolve, enabled = resolveEnabled, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Text("Mark Resolved", modifier = Modifier.padding(start = 6.dp))
            }
        }
        if (showRetry || showEscalate) {
            if (showRetry) {
                OutlinedButton(onClick = onRetry, enabled = retryEnabled, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Text("Retry", modifier = Modifier.padding(start = 6.dp))
                }
            }
            if (showEscalate) {
                OutlinedButton(onClick = onEscalate, enabled = escalateEnabled, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ReportProblem, contentDescription = null)
                    Text("Escalate", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}
