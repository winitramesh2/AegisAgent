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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Psychology

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisApp(viewModel: AegisViewModel = viewModel()) {
    val state by viewModel.ui.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
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
                        Text("Aegis Assistant", fontWeight = FontWeight.Bold)
                        Text(
                            "L1 IAM troubleshooting",
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
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Cloud, contentDescription = null)
                            Text("Cloud Mode")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Psychology, contentDescription = null)
                            Text("Hybrid Mode")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                CoreAiWorkflowScreen(
                    state = state,
                    onQueryChange = viewModel::onQueryChange,
                    onDiagnose = viewModel::submitChat,
                    onRetry = viewModel::retryChat,
                    onEscalate = viewModel::escalateIssue,
                    onMarkResolved = viewModel::markResolved,
                    onToggleAction = viewModel::toggleAction,
                    onUploadLog = { filePicker.launch(arrayOf("text/*", "*/*")) },
                    onLoadTimeline = viewModel::fetchTimeline,
                    onRefreshComponents = viewModel::loadComponentStatus,
                    onCopyStatus = { text -> clipboardManager.setText(AnnotatedString(text)) }
                )
            } else {
                HybridAiWorkflowScreen(
                    state = state,
                    onQueryChange = viewModel::onQueryChange,
                    onRunOnDevice = { viewModel.runHybridDiagnosis(context) },
                    onRunOnCloud = viewModel::submitChat,
                    onRetry = viewModel::retryChat,
                    onEscalate = viewModel::escalateIssue,
                    onMarkResolved = viewModel::markResolved,
                    onToggleAction = viewModel::toggleAction,
                    onUploadLog = { filePicker.launch(arrayOf("text/*", "*/*")) },
                    onLoadTimeline = viewModel::fetchTimeline,
                    onRefreshComponents = viewModel::loadComponentStatus,
                    onCopyStatus = { text -> clipboardManager.setText(AnnotatedString(text)) }
                )
            }
        }
    }
}
