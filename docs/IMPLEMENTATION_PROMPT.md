# Comprehensive Implementation Prompt

You are implementing Aegis Agent, an intelligent L1 support agent for Authenticator Mobile and Windows applications. The MFA apps generate OTP codes and approve login requests. The agent educates users, troubleshoots issues, analyzes logs for root causes, and escalates unresolved cases to JIRA with full context.

## Objectives
- Educate users about MFA usage (OTP generation and approval flows).
- Automate troubleshooting for common issues.
- Analyze logs to identify root cause and fix actions.
- Escalate unresolved issues to JIRA with full context and raw log attachment.

## Scope
**Phase 1 (Core)**
- Cloud-centric logic using Java/Spring Boot + DeepPavlov.
- Native apps (Android/iOS) for chat + log upload.
- JIRA escalation with required fields.

**Phase 2 (Hybrid AI)**
- On-device inference for offline support and lower latency.
- TFLite/Gemini Nano (Android) and Core ML (iOS).
- Local response packs synced from backend.

## Workflow
1. User asks a question or reports a problem.
2. AI classifies intent and confidence.
3. Log analysis detects known error patterns.
4. Provide fix guidance or escalate to JIRA.

## Phase 1 Requirements
### AI Engine (DeepPavlov)
- Run as Docker container.
- BERT intent classifier.
- Training data in `train_data.json` with intents:
  - EnrollmentFailure
  - GenerateOTP
  - TokenSyncError
  - ConfigIssue
  - ServerUnreachable
- `train_model.py` fine-tunes and saves artifacts to a persistent volume.

### Backend Core (Java/Spring Boot)
- `POST /api/chat`
  - Input: user query + device metadata
  - Output: intent + confidence + recommended action
- `POST /api/analyze-logs`
  - Input: log files
  - Output: `{ rootCause, fixAction }`
- Log parsing patterns:
  - Error 503
  - Cert_Invalid
  - Time_Skew

### JIRA Escalation
- Trigger when confidence low or troubleshooting fails.
- Create ticket via JIRA Cloud REST API.
- Required fields:
  - summary
  - priority
  - labels
  - components
  - reporter
  - description (includes chat history, log analysis, device info)
- Attach raw log file using JIRA Attachments API.
- Return ticket ID to user.

### Native Apps (Phase 1)
**Android**
- Kotlin/Compose chat screen.
- Retrofit for networking.
- Collect `Build.MODEL`, `VERSION.SDK_INT`.
- Native file picker for log uploads.

**iOS**
- Swift/SwiftUI chat view.
- URLSession for networking.
- Collect `UIDevice.current` info.
- Native file picker for log uploads.

### Windows App
- Sends the same `/api/chat` and `/api/analyze-logs` payloads.

## Phase 2 Requirements (Hybrid AI)
### Android
- Export a lightweight model to TFLite.
- Use AI Edge SDK or Gemini Nano (AICore) for local inference.
- Local-first logic:
  - confidence > 0.8 -> respond locally from cache
  - confidence <= 0.8 -> call cloud backend

### iOS
- Convert to Core ML (`.mlpackage`).
- Use Natural Language framework.
- Apply the same local-first routing.

### Response Pack Sync
- Mobile apps periodically fetch JSON response packs.
- Local packs allow offline responses without app updates.

## Logging Schema
- Common fields: timestamp, level, app, component, user_id (hashed), device_id (hashed), session_id, request_id
- Mobile fields: os_version, device_model, app_version, network_type
- Backend fields: endpoint, intent, confidence, root_cause, fix_action, jira_ticket_id
- Redact email and phone numbers; hash identifiers.

## Acceptance Criteria
**Phase 1**
- Intent classifier >= 85% validation accuracy.
- Chat API returns intent + confidence + fix guidance.
- Log analyzer resolves known patterns.
- JIRA ticket created with required fields and raw log attachment.
- Android and iOS chat + log upload functional.

**Phase 2**
- Local-first inference works offline for top intents.
- Confidence routing is enforced.
- Response pack sync works without app updates.

## Testing Expectations
- Unit: training pipeline, regex rules, JIRA payload validation.
- Integration: chat pipeline, log upload, ticket creation.
- E2E: user issue resolved, escalation path, offline flow.
- Non-functional: latency < 2s average, PII redaction verified.

## Deliverables
- DeepPavlov training assets
- Java backend services
- Mobile clients (Android/iOS)
- JIRA escalation integration
- Documentation under `docs/system-reminder/`
