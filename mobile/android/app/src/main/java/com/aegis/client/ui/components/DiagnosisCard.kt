package com.aegis.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import kotlin.math.roundToInt

@Composable
fun DiagnosisCard(
    title: String,
    intent: String,
    confidence: Double,
    message: String,
    onCopyClick: (() -> Unit)? = null
) {
    val confidencePercent = (confidence.coerceIn(0.0, 1.0) * 100.0).roundToInt()
    val confidenceLabel = when {
        confidencePercent >= 85 -> "High"
        confidencePercent >= 65 -> "Medium"
        else -> "Low"
    }
    val confidenceColor = when (confidenceLabel) {
        "High" -> Color(0xFF2E7D32)
        "Medium" -> Color(0xFF8D6E00)
        else -> Color(0xFFC62828)
    }
    val confidenceBackground = when (confidenceLabel) {
        "High" -> Color(0xFFE8F5E9)
        "Medium" -> Color(0xFFFFF8E1)
        else -> Color(0xFFFFEBEE)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (onCopyClick != null) {
                    IconButton(onClick = onCopyClick) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy diagnosis")
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp), color = Color(0xFFEFF3F8)) {
                    Text(
                        text = "Issue: ${intent.readableIntent()}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp), color = confidenceBackground) {
                    Text(
                        text = "Confidence: $confidenceLabel ($confidencePercent%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = confidenceColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Text(
                text = "Confidence reflects how certain the model is about this diagnosis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun String.readableIntent(): String {
    return when (this) {
        "GenerateOTP" -> "OTP generation issue"
        "PushApprovalTimeout" -> "Push approval timeout"
        "PasskeyRegistrationFailure" -> "Passkey registration issue"
        "BiometricLockout" -> "Biometric lockout"
        "ServerUnreachable" -> "Server connectivity issue"
        "ConfigIssue" -> "Configuration issue"
        "EnrollmentFailure" -> "Enrollment issue"
        "TokenSyncError" -> "Token sync issue"
        "TimeDriftFailure" -> "Device time drift issue"
        "Unknown" -> "Unclear issue"
        else -> this
    }
}
