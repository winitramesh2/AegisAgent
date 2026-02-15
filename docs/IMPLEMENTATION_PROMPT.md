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
- Use open-source and free-to-use frameworks by default.

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
2. Backend sends query to cloud LLM for primary diagnosis and action plan.
3. Backend cross-verifies diagnosis with local DeepPavlov model.
4. Agent displays clear diagnosis and bulleted actions.
5. If unresolved, user presses Retry.
6. Retry request appends prior diagnosis/actions and calls cloud LLM only.
7. If still unresolved, user presses Escalate.
8. Backend creates JIRA ticket, sends email, and returns 3-working-days SLA message.

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
  - PushApprovalTimeout
  - PasskeyRegistrationFailure
  - BiometricLockout
  - TimeDriftFailure
  - DeviceBindingFailure
- `train_model.py` fine-tunes and stores artifacts in persistent volume.

### 5.2 Backend Core (Java/Spring Boot)
- `POST /api/chat`
  - Input: user query + device metadata + `retryAttempt`
  - Output: intent + confidence + diagnosis + recommended actions
  - Behavior:
    - `retryAttempt=false` -> cloud-first + DeepPavlov cross-verification
    - `retryAttempt=true` -> cloud-only retry path
- `POST /api/analyze-logs`
  - Input: log file
  - Output: `{ rootCause, fixAction }`
- `POST /api/escalate`
  - Input: unresolved issue context (JSON or multipart)
  - Output: escalation status + ticket ID + SLA message
- Detect known log patterns:
  - `Error 503`
  - `Cert_Invalid`
  - `Time_Skew`
- Add IAM-specific analyzers for:
  - TOTP/HOTP validation and seed sync failures
  - FIDO2/WebAuthn registration/assertion failures
  - Push timeout/approval mismatch and biometric lockout

### 5.2.1 Log and Search Platform
- Use OpenSearch Stack as default:
  - OpenSearch
  - OpenSearch Dashboards
  - Data Prepper
- Correlate events by `request_id`, `session_id`, `challenge_id`, and `idp_event_id`.

### 5.2.2 IdP Connector Layer (Extensible)
- Okta System Log adapter (recommended).
- Microsoft Entra sign-in log adapter (recommended).
- Interface-ready adapters for Ping and Google identity sources.

### 5.3 Email + JIRA Escalation
- Trigger only when user explicitly presses Escalate after guided and retry responses.
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
- Return SLA text: support team will respond within 3 working days.
- Enforce escalation quality gate requiring:
  - sanitized evidence bundle
  - attempted fix actions
  - confidence score + correlation IDs

### 5.4 Native App Integrations

**Android**
- Kotlin/Compose chat UI
- Retrofit networking
- Capture `Build.MODEL`, `VERSION.SDK_INT`
- Native file picker for logs
- Add `Retry` and `Escalate` actions in the chat panel
- On Retry, append previous diagnosis and attempted action list before sending
- Render diagnosis and actions in clear bullets

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
- IAM fields: `auth_protocol`, `auth_event_type`, `challenge_id`, `policy_result`, `risk_level`, `idp_provider`, `idp_event_id`
- Redact phone/email and sensitive tokens before storage or escalation.

---

## 8) Acceptance Criteria

### Phase 1
- Intent classifier accuracy >= 85%.
- Chat API returns intent + confidence + action.
- Log analyzer resolves known patterns.
- Email + JIRA escalation works with raw log attachment.
- Client Apps (Android, iOS and Desktop) can upload logs.
- OpenSearch dashboard correlation works for incident replay.
- False escalation rate is tracked and reduced across pilot iterations.

### Phase 2
- Local-first inference works offline for top intents.
- Confidence threshold routing is enforced.
- Response pack sync updates without app release.

---

## 9) Testing Expectations

- Unit: training pipeline, regex rules, escalation payload validation.
- Integration: chat flow, log upload flow, email/JIRA flow, connector flow (if enabled).
- E2E: issue resolution path, escalation path, offline path.
- Non-functional: average latency < 2s, PII redaction validated.
- Safety: non-destructive guidance checks and human override verification.

---

## 10) Deliverables

- DeepPavlov training assets
- Java backend services
- Android, iOS, and Windows client integrations
- Email + JIRA escalation integration
- Documentation under `docs/`
