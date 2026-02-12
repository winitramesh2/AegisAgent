# Architecture

## System Overview
Aegis Agent combines a cloud AI service, a Java/Spring Boot backend, and native mobile clients. It ingests user queries and logs, performs intent classification and log analysis, then resolves issues or escalates to JIRA with full context.

## Architecture Diagram
```mermaid
flowchart LR
  User[User] --> Mobile[Mobile Apps]
  User --> Windows[Windows App]

  subgraph Mobile Apps
    Android[Android: Kotlin/Compose]
    iOS[iOS: Swift/SwiftUI]
  end

  Mobile --> API[Java/Spring Boot API]
  Windows --> API

  API --> AI[DeepPavlov BERT]
  API --> Logs[Log Analysis Engine]
  API --> JIRA[JIRA Cloud REST]

  Logs --> Result[Root Cause + Fix Action]
  AI --> Result
  Result --> API
  JIRA --> Ticket[Ticket ID + Attachment]
  API --> Mobile
  API --> Windows
```

## Flow Chart
```mermaid
flowchart TD
  Start[User Query] --> Intent[Intent Classification]
  Intent --> Known{Known Issue?}
  Known -->|Yes| Fix[Provide Fix Action]
  Known -->|No| Logs[Analyze Logs]
  Logs --> Resolved{Resolved?}
  Resolved -->|Yes| Fix
  Resolved -->|No| Escalate[Escalate to JIRA]
  Escalate --> End[Return Ticket ID]
```

## Sequence Diagram
```mermaid
sequenceDiagram
  participant U as User
  participant M as Mobile App
  participant A as API Gateway
  participant D as DeepPavlov
  participant L as Log Analyzer
  participant J as JIRA

  U->>M: Submit issue + logs
  M->>A: /api/chat + /api/analyze-logs
  A->>D: Intent classification
  D-->>A: Intent + confidence
  A->>L: Log parsing
  L-->>A: rootCause + fixAction
  alt Resolved
    A-->>M: Fix guidance
  else Unresolved
    A->>J: Create ticket + attach logs
    J-->>A: Ticket ID
    A-->>M: Ticket ID
  end
```

## Tech Stack
- Backend: Java, Spring Boot, REST APIs
- AI Engine: DeepPavlov (BERT intent classifier)
- Log Analysis: Java regex-based parser
- Mobile Apps: Android (Kotlin/Compose), iOS (Swift/SwiftUI)
- JIRA: Cloud REST API + Attachments API
- Phase 2: Android TFLite/Gemini Nano, iOS Core ML

## JIRA Escalation Payload (Required Fields)
- summary
- priority
- labels
- components
- reporter
- description (auto-built from chat + logs + device context)
- attachment (raw log file)

## Phase 2 Hybrid Intelligence
Local-first intent classification is used for offline support and lower latency. If local confidence is below threshold, the request is forwarded to the cloud model.
