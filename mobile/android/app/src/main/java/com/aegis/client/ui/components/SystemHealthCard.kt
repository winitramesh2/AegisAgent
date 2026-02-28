package com.aegis.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aegis.client.network.ComponentStatusItem
import com.aegis.client.network.ComponentStatusResponse

@Composable
fun SystemHealthCard(
    componentStatus: ComponentStatusResponse?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("System Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (showDetails) "Hide" else "Show",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = { showDetails = !showDetails }) {
                        Icon(
                            imageVector = if (showDetails) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle details"
                        )
                    }
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        Icon(Icons.Filled.Sync, contentDescription = "Refresh system health")
                    }
                }
            }

            if (componentStatus == null) {
                Text("System status unavailable.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                componentStatus.components.toSortedMap().forEach { (name, item) ->
                    MiniStatusChip(name = name, item = item)
                }
            }

            if (showDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    componentStatus.components.toSortedMap().forEach { (name, item) ->
                        DetailStatusRow(name = name, item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStatusRow(name: String, item: ComponentStatusItem) {
    val running = item.status.equals("UP", ignoreCase = true) || item.status.equals("RUNNING", ignoreCase = true)
    val color = if (running) Color(0xFF2E7D32) else Color(0xFFC62828)
    val background = if (running) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(iconForComponent(name), contentDescription = null, tint = color)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val detailText = item.detail?.let { " • $it" } ?: ""
            Text(
                text = "${name.uppercase()} • ${if (running) "Running" else "Down"}$detailText",
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
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
        }
    }
}

@Composable
private fun MiniStatusChip(name: String, item: ComponentStatusItem) {
    val running = item.status.equals("UP", ignoreCase = true) || item.status.equals("RUNNING", ignoreCase = true)
    val color = if (running) Color(0xFF2E7D32) else Color(0xFFC62828)
    val background = if (running) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Row(
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, shape = CircleShape))
        Icon(iconForComponent(name), contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(
            text = "${name.uppercase().take(5)} ${if (running) "OK" else "DOWN"}",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun iconForComponent(name: String): ImageVector {
    return when (name.lowercase()) {
        "backend" -> Icons.Filled.Dns
        "deeppavlov" -> Icons.Filled.Psychology
        "opensearch" -> Icons.Filled.Storage
        "jira" -> Icons.Filled.ConfirmationNumber
        "email" -> Icons.Filled.Email
        else -> Icons.Filled.Storage
    }
}
