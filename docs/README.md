# 🛡️ Aegis Agent

> **AI enabled intelligent L1 support Agent for Mobile and Windows Authenticator Apps**  
> *Guides users through OTP, passkey, and approval flows, troubleshoots issues, analyzes logs for root causes, and escalates unresolved cases to email and JIRA with full context.*

![Backend](https://img.shields.io/badge/Backend-Java%20%7C%20Spring%20Boot-blue?style=for-the-badge)
![AI](https://img.shields.io/badge/AI-DeepPavlov%20BERT%20%7C%20Cloud-green?style=for-the-badge)
![Mobile](https://img.shields.io/badge/Plateform-Android%20%7C%20iOS%20%7C%20Windows-lightgrey?style=for-the-badge)
![Phase2](https://img.shields.io/badge/On%20Device%20AI-TFLite%20%7C%20Core%20ML%20%7C%20Windows%20ML-orange?style=for-the-badge)

- **Purpose**: Decision-ready recommendations
- **Problem**: Customers need 24/7 support for authenticator app issues (OTP, biometrics, passkeys), but manual support is slow and expensive.
- **Solution**: AI-powered L1 support bot that understands issues (DeepPavlov BERT), analyzes logs (OpenSearch Stack pattern matching + correlation), delivers instant fixes (playbooks), escalates with context (SMTP + JIRA), and enables offline support (TFLite, Core ML, Windows ML).
- **Competitive edge**: First MFA-specialized bot with offline capability and forensic log analysis; a gap in current Okta/Microsoft/Ping offerings.
- **Audience**: Users, Engineering Team, Product Leadership

---

## ✅ What It Delivers
| Capability | Outcome |
| --- | --- |
| User education | Clear guidance on OTP, passkeys, and approvals |
| Troubleshooting | Fast resolution with log-driven diagnostics |
| Escalation | Email + JIRA tickets with full context and attachments |

## ⚙️ How It Works
1. User reports an issue.
2. Cloud LLM generates the primary diagnosis and first-resolution actions.
3. DeepPavlov cross-verifies the diagnosis to refine confidence and actions.
4. User can press Retry to run a second-pass cloud-only diagnosis using prior attempted actions.
5. If still unresolved, user presses Escalate to create JIRA + send email with full context.
6. User receives SLA confirmation: support will respond within 3 working days.

## 🧭 Build Path (Short)
| Step | Focus | Outcome |
| --- | --- | --- |
| 1 | Define scope | OTP/passkey education, log analysis, escalation |
| 2 | Build core | Java API + DeepPavlov intents + log parser |
| 3 | Integrate | SMTP email, JIRA tickets, log storage |
| 4 | Ship clients | Android, iOS, Windows surfaces |
| 5 | Test and iterate | Improvements from real logs and feedback |

## 🧩 Architecture Snapshot
```mermaid
flowchart LR
  User[User] --> Client[Client Apps]
  Client --- Platforms[Android / iOS / Desktop]
  Client --> API[Java/Spring Boot API]
  API --> Cloud[Cloud LLM]
  API --> AI[DeepPavlov BERT]
  API --> Logs[Log Analysis Engine]
  API --> Email[SMTP Email Escalation]
  API --> JIRA[JIRA Cloud REST]
  Cloud --> Result[Primary Diagnosis + Actions]
  AI --> Result2[Cross-Verified Diagnosis]
  Result2 --> Result
  Logs --> Result
  Result --> API
```

## 🗺️ Phase Roadmap
| # | Focus | Outcome |
| --- | --- | --- |
| 1: Core AI | Java + DeepPavlov + Native Apps + JIRA | Cloud-first troubleshooting and escalation |
| 2: Hybrid AI | On-device AI + response packs | Offline capability and lower latency |

## 🧱 Tech Stack (IAM-Focused)
- Backend: Java 17, Spring Boot 3.x, REST APIs
- AI Engine (Cloud): DeepPavlov (BERT intent classifier)
- On-Device AI:
  - Android: TensorFlow Lite (quantized INT8, 28 MB)
  - iOS: Core ML (FP16, 55 MB)
  - Windows: Windows ML (ONNX Runtime, 110 MB)
- Log Analysis: OpenSearch Stack (OpenSearch + OpenSearch Dashboards + Data Prepper)
- Client Apps: Android (Kotlin/Compose), iOS (Swift/SwiftUI), Windows (WinUI 3)
- Email: SMTP
- JIRA: Cloud REST API + Attachments API

## 🧠 IAM Domain Coverage
- Protocols and factors: OTP (TOTP/HOTP), FIDO2/WebAuthn passkeys, biometrics, push approvals.
- IAM event focus: sign-in failures, enrollment failures, challenge timeouts, policy denials, and device trust issues.
- Platform focus: Android, iOS, and Desktop client diagnostics with shared escalation format.

## 📈 Quality Gates
- First response is always guided troubleshooting (no auto-escalation on first pass).
- Retry pass uses cloud-only diagnosis with appended prior actions and outcomes.
- Escalate only when user confirms unresolved status through explicit Escalate action.
- Escalation bundle includes sanitized logs, device metadata, error timeline, and attempted fixes.
- Success is measured by first-contact resolution, false escalation rate, and root-cause precision.

## 🔐 Security and Responsible AI
| Area | How it is secured and responsible |
| --- | --- |
| Data protection | PII is redacted, identifiers are hashed, and logs are encrypted in transit and at rest. |
| Access control | Role-based access is enforced with least-privilege API keys and scoped JIRA tokens. |
| Auditability | Immutable audit logs track classifications, actions, and escalations end-to-end. |
| Safety | Guidance avoids destructive steps that could lock users out. |
| Transparency | Confidence levels are surfaced and missing context is requested before escalation. |
| Compliance | GDPR and HIPAA alignment via data minimization, retention limits, and access logging. |

## 🧭 Competitive Differentiation
3 major gaps to exploit that Okta/Microsoft/Ping do not address:
1. Offline-first MFA support: on-device AI works without internet.
2. Forensic log analysis: automated root cause plus sanitized log bundles.
3. MFA-specialized expertise: deep OTP, FIDO2, and biometric knowledge.

## 🔗 Quick Links
- Repository: `https://github.com/winitramesh2/AegisAgent`
- Architecture: `docs/ARCHITECTURE.md`
- Implementation Guide: `docs/IMPLEMENTATION_GUIDE.md`
- Implementation Prompt: `docs/IMPLEMENTATION_PROMPT.md`
- Diagrams: `docs/DIAGRAMS.md`
