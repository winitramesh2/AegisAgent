# Detailed Implementation Guide

This guide is written for beginners and walks through the Phase 1 core build and the Phase 2 hybrid AI extension.

## Phase 1: Core Foundation

### 1) AI Engine (DeepPavlov)
**Goal**: Cloud model is the source of truth for complex queries.

**Steps**
1. Deploy DeepPavlov in a Docker container.
2. Create `train_data.json` with labeled intents:
   - EnrollmentFailure
   - GenerateOTP
   - TokenSyncError
   - ConfigIssue
   - ServerUnreachable
3. Implement `train_model.py` to:
   - Load training data
   - Fine-tune the BERT classifier
   - Save model artifacts to a persistent volume
4. Expose an inference endpoint for intent + confidence.

### 2) Backend Core (Java/Spring Boot)
**API Gateway**
- `POST /api/chat`
  - Input: user query, app metadata, device info
  - Output: intent, confidence, recommended action
- `POST /api/analyze-logs`
  - Input: log file
  - Output: `{ rootCause, fixAction }`

**Log Analysis Engine**
- Regex patterns:
  - `Error 503` -> "Service unavailable or upstream outage"
  - `Cert_Invalid` -> "Device certificate invalid or expired"
  - `Time_Skew` -> "Device time out of sync"
- Return JSON for troubleshooting responses.

**JIRA Escalation Service**
- Trigger escalation when:
  - Confidence below threshold
  - Log analysis returns unknown root cause
  - User still fails after guided steps
- Required fields for ticket creation:
  - summary
  - priority
  - labels
  - components
  - reporter
  - description (auto-built from chat history + device data + logs)
- Attach raw log file via JIRA Attachments API.
- Return ticket ID to the user (example: `AEGIS-1234`).

### 3) Mobile Clients (Android + iOS)
**Android (Kotlin/Compose)**
- Build `ChatScreen` and integrate Retrofit.
- Capture `Build.MODEL`, `VERSION.SDK_INT`.
- Add a native file picker to upload logs.

**iOS (Swift/SwiftUI)**
- Build `ChatView` and integrate URLSession.
- Capture `UIDevice.current` metadata.
- Add a native file picker to upload logs.

**Windows App**
- Send user queries and log files to the same backend APIs.
- Use identical payload structure to keep consistency across platforms.

## Phase 2: Hybrid AI (On-Device)

### 1) Android Local-First AI
- Export a lightweight intent model to TFLite.
- Integrate via AI Edge SDK or Gemini Nano (AICore).
- If confidence > 0.8:
  - Serve cached response locally.
- Else:
  - Fallback to cloud model.

### 2) iOS Local-First AI
- Convert model to Core ML (`.mlpackage`).
- Use Natural Language framework for inference.
- Same confidence routing logic as Android.

### 3) Local Response Pack Sync
- Define a JSON response pack format (intent -> response).
- Mobile apps periodically fetch updates from backend.
- Use cached responses for offline support.

## Recommended Logging Schema
**Common Fields**
- timestamp (ISO 8601)
- level (INFO/WARN/ERROR)
- app (android/ios/backend/windows)
- component (chat/log-analyzer/jira-service)
- user_id (hashed)
- device_id (hashed)
- session_id
- request_id

**Mobile Fields**
- os_version
- device_model
- app_version
- network_type (wifi/cell/offline)

**Backend Fields**
- endpoint
- intent
- confidence
- root_cause
- fix_action
- jira_ticket_id

**PII Safety**
- Redact phone and email.
- Hash user identifiers.

## Acceptance Criteria
**Phase 1**
- Intent model accuracy >= 85% on validation.
- `/api/chat` returns structured response with confidence.
- `/api/analyze-logs` resolves known patterns.
- JIRA ticket creation includes required fields and raw log attachment.
- Android and iOS apps can upload logs and send metadata.

**Phase 2**
- Local-first inference works offline for top intents.
- Confidence routing is enforced.
- Response Pack sync works without app updates.

## Test Plan
**Unit Tests**
- Training pipeline data validation.
- Regex log parsing coverage.
- JIRA payload validation.

**Integration Tests**
- Chat API -> intent classification -> response.
- Log upload -> analyzer -> response.
- JIRA ticket creation + attachment.

**E2E Tests**
- User issue -> resolution without escalation.
- User issue -> escalation with ticket ID.
- Offline mode with local response pack.

**Non-Functional Tests**
- Latency: average response < 2 seconds.
- Security: PII redaction verified.
