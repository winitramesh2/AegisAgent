package com.aegis.client.ui

enum class WorkflowStage {
    Draft,
    Diagnosed,
    ActionsInProgress,
    RetryResult,
    Escalated,
    Resolved
}

data class ActionChecklistItem(
    val label: String,
    val done: Boolean = false
)
