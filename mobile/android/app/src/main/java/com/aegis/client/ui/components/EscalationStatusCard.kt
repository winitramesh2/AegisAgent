package com.aegis.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EscalationStatusCard(
    ticketId: String?,
    message: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Escalation Submitted", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            ticketId?.let {
                Text("Ticket: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
