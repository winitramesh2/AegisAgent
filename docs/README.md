# Aegis Agent - Authenticator App Support Bot

AI-powered L1 support bot for Authenticator Mobile and Windows apps. Helps users resolve OTP, passkey, and approval issues with log-based guidance and fast escalation.

- **Date**: Feb 14, 2026
- **For**: Users, Engineering, Product
- **Purpose**: Decision-ready summary
- **Problem**: 24/7 MFA support is costly and slow to scale
- **Solution**: DeepPavlov intents + ELK log analysis + playbooks + SMTP/JIRA escalation + offline support (TFLite/Core ML/Windows ML)
- **Edge**: MFA-specialized with offline capability and forensic log analysis (gap vs Okta/Microsoft/Ping)

---

## What It Delivers
| Capability | Outcome |
| --- | --- |
| User education | Clear guidance on OTP, passkeys, and approvals |
| Troubleshooting | Fast resolution with log-driven diagnostics |
| Escalation | Email + JIRA tickets with full context and attachments |

## How It Works
1. User reports an issue.
2. Intent is classified with device context.
3. Logs are analyzed for known patterns.
4. Guidance is returned or escalated to email + JIRA.

## Build Path (Short)
| Step | Focus | Outcome |
| --- | --- | --- |
| 1 | Define scope | OTP/passkey education, log analysis, escalation |
| 2 | Build core | Java API + DeepPavlov intents + log parser |
| 3 | Integrate | SMTP email, JIRA tickets, log storage |
| 4 | Ship clients | Android, iOS, Windows surfaces |
| 5 | Test and iterate | Improvements from real logs and feedback |

## Architecture Snapshot
```mermaid
flowchart LR
  User[User] --> Client[Mobile or Windows App]
  Client --> API[Java/Spring Boot API]
  API --> AI[DeepPavlov BERT]
  API --> Logs[Log Analysis Engine]
  API --> Email[SMTP Email Escalation]
  API --> JIRA[JIRA Cloud REST]
  Logs --> Result[Root Cause + Fix Action]
  AI --> Result
  Result --> API
```

## Phase Roadmap
| Phase | Focus | Outcome |
| --- | --- | --- |
| Phase 1: Core | Java + DeepPavlov + Native Apps + JIRA | Cloud-first troubleshooting and escalation |
| Phase 2: Hybrid AI | On-device AI + response packs | Offline capability and lower latency |

## Security and Responsible AI
| Area | How it is secured and responsible |
| --- | --- |
| Data protection | PII is redacted, identifiers are hashed, and logs are encrypted in transit and at rest. |
| Access control | Role-based access is enforced with least-privilege API keys and scoped JIRA tokens. |
| Auditability | Immutable audit logs track classifications, actions, and escalations end-to-end. |
| Safety | Guidance avoids destructive steps that could lock users out. |
| Transparency | Confidence levels are surfaced and missing context is requested before escalation. |
| Compliance | GDPR and HIPAA alignment via data minimization, retention limits, and access logging. |

## Quick Links
- Architecture: `docs/ARCHITECTURE.md`
- Implementation Guide: `docs/IMPLEMENTATION_GUIDE.md`
- Implementation Prompt: `docs/IMPLEMENTATION_PROMPT.md`
- Diagrams: `docs/DIAGRAMS.md`
