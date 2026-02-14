# Implementation Prompt: Aegis Agent

> Use this as the master build prompt for engineering and AI-assisted implementation.

## 1) System Role and Objective

You are implementing **Aegis Agent**, an intelligent L1 support assistant for Authenticator Mobile and Windows applications. The apps support OTP, passkeys, and login approvals. The assistant educates users, troubleshoots issues, analyzes logs, and escalates unresolved cases to email and JIRA with full context.

---

## 2) Core Outcomes

- Educate users on OTP/passkey/approval workflows.
- Detect and classify common support intents.
- Analyze logs to identify root cause and fix action.
- Escalate unresolved issues to email and JIRA with raw log attachment.

---

## 3) Scope

### Phase 1 (Core)
- Cloud-centric backend with Java/Spring Boot + DeepPavlov.
- Native app integrations for Android, iOS, Windows.
- Email + JIRA escalation flow.

### Phase 2 (Hybrid AI)
- On-device inference for low latency and offline support.
- Android: TensorFlow Lite
- iOS: Core ML
- Windows: Windows ML (ONNX Runtime)
- Response packs synced from backend.

---

## 4) User Workflow

1. User asks a question or reports an issue.
2. AI classifies intent and confidence.
3. Log analysis detects known patterns.
4. Agent provides fix guidance or escalates to email and JIRA.

---

## 5) Phase 1 Requirements

### 5.1 AI Engine (DeepPavlov)
- Run in Docker.
- Use BERT intent classifier.
- Train with `train_data.json` using intents:
  - EnrollmentFailure
  - GenerateOTP
  - TokenSyncError
  - ConfigIssue
  - ServerUnreachable
- `train_model.py` fine-tunes and stores artifacts in persistent volume.

### 5.2 Backend Core (Java/Spring Boot)
- `POST /api/chat`
  - Input: user query + device metadata
  - Output: intent + confidence + recommended action
- `POST /api/analyze-logs`
  - Input: log file
  - Output: `{ rootCause, fixAction }`
- Detect known log patterns:
  - `Error 503`
  - `Cert_Invalid`
  - `Time_Skew`

### 5.3 Email + JIRA Escalation
- Trigger when confidence is low or troubleshooting fails.
- Send escalation email with summary and troubleshooting context.
- Create JIRA issue via Cloud REST API.
- Include required fields:
  - summary
  - priority
  - labels
  - components
  - reporter
  - description (chat history + log analysis + device context)
- Attach raw log with JIRA Attachments API.
- Return ticket ID to user.

### 5.4 Native App Integrations

**Android**
- Kotlin/Compose chat UI
- Retrofit networking
- Capture `Build.MODEL`, `VERSION.SDK_INT`
- Native file picker for logs

**iOS**
- Swift/SwiftUI chat UI
- URLSession networking
- Capture `UIDevice.current` metadata
- Native file picker for logs

**Windows**
- WinUI 3 surface
- Same payload structure as mobile for `/api/chat` and `/api/analyze-logs`

---

## 6) Phase 2 Requirements (Hybrid AI)

### 6.1 Android
- TFLite model (quantized INT8, target ~28 MB)
- Local-first logic:
  - confidence `> 0.8` -> local response
  - confidence `<= 0.8` -> cloud fallback

### 6.2 iOS
- Core ML model (FP16, target ~55 MB)
- Same local-first confidence routing

### 6.3 Windows
- Windows ML via ONNX Runtime (target ~110 MB)
- Same local-first confidence routing

### 6.4 Response Pack Sync
- Periodically fetch JSON response packs from backend.
- Use local packs for offline answers without app updates.

---

## 7) Logging and Data Safety

- Common log fields: `timestamp`, `level`, `app`, `component`, `user_id` (hashed), `device_id` (hashed), `session_id`, `request_id`
- Client fields: `os_version`, `device_model`, `app_version`, `network_type`
- Backend fields: `endpoint`, `intent`, `confidence`, `root_cause`, `fix_action`, `jira_ticket_id`
- Redact phone/email and sensitive tokens before storage or escalation.

---

## 8) Acceptance Criteria

### Phase 1
- Intent classifier accuracy >= 85%.
- Chat API returns intent + confidence + action.
- Log analyzer resolves known patterns.
- Email + JIRA escalation works with raw log attachment.
- Android/iOS/Windows clients can upload logs.

### Phase 2
- Local-first inference works offline for top intents.
- Confidence threshold routing is enforced.
- Response pack sync updates without app release.

---

## 9) Testing Expectations

- Unit: training pipeline, regex rules, escalation payload validation.
- Integration: chat flow, log upload flow, email/JIRA flow.
- E2E: issue resolution path, escalation path, offline path.
- Non-functional: average latency < 2s, PII redaction validated.

---

## 10) Deliverables

- DeepPavlov training assets
- Java backend services
- Android, iOS, and Windows client integrations
- Email + JIRA escalation integration
- Documentation under `docs/`
