package com.aegis.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IncidentInputCard(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    helperText: String,
    primaryLabel: String,
    primaryIcon: ImageVector,
    onPrimaryClick: () -> Unit,
    primaryEnabled: Boolean,
    secondaryLabel: String? = null,
    secondaryIcon: ImageVector? = null,
    onSecondaryClick: (() -> Unit)? = null,
    secondaryEnabled: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp),
                minLines = 4,
                maxLines = 7,
                label = { Text("Issue details") },
                placeholder = { Text(placeholder) },
                singleLine = false
            )
            Text(helperText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPrimaryClick,
                    enabled = primaryEnabled,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(primaryIcon, contentDescription = null)
                    Text(primaryLabel, modifier = Modifier.padding(start = 6.dp))
                }
                if (secondaryLabel != null && secondaryIcon != null && onSecondaryClick != null) {
                    OutlinedButton(
                        onClick = onSecondaryClick,
                        enabled = secondaryEnabled,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(secondaryIcon, contentDescription = null)
                        Text(secondaryLabel, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }
    }
}
