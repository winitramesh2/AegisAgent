# Implementation Guide: Aegis Agent

> Beginner-friendly build guide for Phase 1 (cloud core) and Phase 2 (hybrid on-device AI).

## 1) Objective

Build an intelligent L1 support bot for Authenticator Mobile and Windows apps that can educate users, troubleshoot common MFA issues, analyze logs, and escalate unresolved issues to email and JIRA.

**Build principle**: open-source and free-to-use components by default.

---

## 2) Phase 1 - Core Foundation

### 2.1 AI Engine (DeepPavlov)

**Goal**: Keep the cloud model as the source of truth for complex intent classification.

**Steps**
1. Deploy DeepPavlov in Docker.
2. Create `train_data.json` with labeled intents:
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
3. Implement `train_model.py` to:
   - Load training data
   - Fine-tune BERT classifier
   - Save artifacts to persistent volume
4. Expose inference endpoint returning intent + confidence.

### 2.2 Backend Core (Java/Spring Boot)

**Support workflow contract**
- First pass: cloud diagnosis first, then DeepPavlov cross-verification.
- Retry pass: cloud-only diagnosis using the original query plus prior diagnosis/actions.
- Escalation: user-triggered only through explicit Escalate action.

**API Gateway**
- `POST /api/chat`
  - Input: user query, app metadata, device info, `retryAttempt`
  - Output: intent, confidence, diagnosis, recommended actions
- `POST /api/analyze-logs`
  - Input: log file
  - Output: `{ rootCause, fixAction }`
- `POST /api/escalate`
  - Input: user query + context (JSON or multipart with optional log file)
  - Output: escalation status + JIRA ticket ID + SLA message

**Log Analysis Engine**
- Parse known errors and IAM event markers:
  - `Error 503` -> Service unavailable or upstream outage
  - `Cert_Invalid` -> Device certificate invalid or expired
  - `Time_Skew` -> Device time out of sync
- Add IAM protocol markers:
  - TOTP/HOTP challenge and seed sync failures
  - FIDO2/WebAuthn registration/assertion failures
  - Push timeout and approval mismatch events
- Return structured troubleshooting JSON with confidence and evidence IDs.

**OpenSearch Pipeline (recommended default)**
- Ingest logs using Data Prepper (or Logstash) into OpenSearch indices.
- Build dashboards for support, escalation triage, and incident timelines.
- Correlate events by `request_id`, `session_id`, and `challenge_id`.

**IdP Connector Layer (optional, recommended)**
- Okta System Log adapter.
- Microsoft Entra sign-in log adapter.
- Extensible adapter interface for Ping and Google identity sources.

**Email + JIRA Escalation Service**
- Trigger escalation when user presses Escalate after guided and retry attempts.
- Send escalation email summary with issue context.
- Create JIRA ticket with required fields:
  - summary
  - priority
  - labels
  - components
  - reporter
  - description (chat + logs + device context)
- Attach raw log file via JIRA Attachments API.
- Return ticket ID to user (for example: `AEGIS-1234`).
- Return user-facing SLA: support team will respond within 3 working days.
- Enforce escalation quality gate:
  - include sanitized logs
  - include attempted fixes
  - include root cause confidence and correlation keys

### 2.3 Client Apps (Android, iOS, Windows)

**Android (Kotlin/Compose)**
- Build `ChatScreen` and integrate Retrofit.
- Capture `Build.MODEL` and `VERSION.SDK_INT`.
- Add native file picker for log uploads.
- Add action buttons: `Send Chat`, `Retry`, `Escalate`.
- On Retry, append previous diagnosis and action list to the original query before sending.
- Render diagnosis and action points clearly in bullet format.

**iOS (Swift/SwiftUI)**
- Build `ChatView` and integrate URLSession.
- Capture `UIDevice.current` metadata.
- Add native file picker for log uploads.

**Windows (WinUI 3)**
- Send queries/logs to same backend APIs.
- Keep payload format consistent across Client Apps (Android, iOS and Desktop).

---

## 3) Phase 2 - Hybrid On-Device AI

### 3.1 Android Local-First Inference
- Deploy TensorFlow Lite model (quantized INT8, target ~28 MB).
- If confidence `> 0.8`: return local cached response.
- Else: fallback to cloud model.

### 3.2 iOS Local-First Inference
- Deploy Core ML model (FP16, target ~55 MB).
- Apply same confidence routing as Android.

### 3.3 Windows Local-First Inference
- Deploy Windows ML model via ONNX Runtime (target ~110 MB).
- Apply same confidence routing and fallback logic.

### 3.4 Local Response Pack Sync
- Define JSON response pack (`intent -> response`).
- Apps fetch updates periodically from backend.
- Cached packs support offline troubleshooting without app update.

---

## 4) Logging Schema

**Common fields**
- timestamp (ISO 8601)
- level (INFO/WARN/ERROR)
- app (android/ios/windows/backend)
- component (chat/log-analyzer/jira-service)
- user_id (hashed)
- device_id (hashed)
- session_id
- request_id

**Client App fields (Android, iOS and Desktop)**
- os_version
- device_model
- app_version
- network_type (wifi/cell/offline)

**Backend fields**
- endpoint
- intent
- confidence
- root_cause
- fix_action
- jira_ticket_id

**IAM analysis fields**
- auth_protocol (`totp`, `hotp`, `fido2`, `push`, `biometric`)
- auth_event_type
- challenge_id
- policy_result
- risk_level
- idp_provider
- idp_event_id

**PII safety**
- Redact phone and email.
- Hash user identifiers.

---

## 5) Acceptance Criteria

**Phase 1**
- Intent model accuracy >= 85% on validation set.
- `/api/chat` returns structured response with confidence.
- First pass executes cloud-first + DeepPavlov cross-verification.
- Retry pass executes cloud-only inference path.
- `/api/analyze-logs` resolves known patterns.
- Email escalation sends summary payload.
- JIRA ticket includes required fields and raw log attachment.
- `/api/escalate` returns ticket ID and 3-working-days SLA message.
- Client Apps (Android, iOS and Desktop) can upload logs and send metadata.
- OpenSearch dashboards show end-to-end correlation for test incidents.
- False escalation rate target <= 20% in pilot.

**Phase 2**
- Local-first inference works offline for top intents.
- Confidence routing is enforced consistently.
- Response pack sync works without app updates.

---

## 6) Test Plan

**Unit tests**
- Training data + model pipeline validation
- Regex parser coverage for known error signatures
- JIRA payload and attachment validation
- IAM intent confusion-matrix checks

**Integration tests**
- Chat API -> intent inference -> response
- Log upload -> analyzer -> troubleshooting output
- Email escalation + JIRA ticket creation
- Connector adapter tests (Okta/Entra if enabled)

**E2E tests**
- Issue resolved without escalation
- Issue escalated with ticket ID
- Offline handling with local response pack

**Non-functional tests**
- Average response latency < 2 seconds
- PII redaction and access control verification
- Safety validation for non-destructive guidance
