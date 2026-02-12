# Diagrams

## System Architecture
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

## Troubleshooting Flow
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

## Escalation Sequence
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
