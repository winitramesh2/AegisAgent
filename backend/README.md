# Aegis Agent Backend (Phase 1)

Spring Boot API for chat triage, log analysis, and escalation.

## Endpoints

- `POST /api/chat` - classify issue intent and return first-aid actions or escalation ticket.
- `POST /api/analyze-logs` - parse uploaded log file and return `rootCause` and `fixAction`.
- `POST /api/escalate` - send escalation email and create JIRA issue with optional raw log attachment.
- `GET /api/incidents/{correlationId}` - fetch timeline events indexed in OpenSearch.
- `GET /api/incidents` - fetch incident timeline by filters (`platform`, `eventType`, `from`, `to`, `size`).
- `GET /api/admin/jira/validate` - validate configured JIRA issue mappings (issue type, fields, components).

## Run

```bash
mvn spring-boot:run
```

## Environment

- `JIRA_BASE_URL`, `JIRA_USER`, `JIRA_API_TOKEN`
- `JIRA_PRIORITY`, `JIRA_LABELS`, `JIRA_COMPONENTS`, `JIRA_REPORTER_ACCOUNT_ID`
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`
- `AEGIS_ESCALATION_EMAIL_TO`
- `DEEPPAVLOV_ENABLED`, `DEEPPAVLOV_URL`
- `OPENSEARCH_ENABLED`, `OPENSEARCH_URL`, `OPENSEARCH_USER`, `OPENSEARCH_PASSWORD`, `OPENSEARCH_INDEX`
- `JIRA_VALIDATE_ON_STARTUP`, `JIRA_FAIL_ON_VALIDATION`

## Notes

- Intent classification uses DeepPavlov when enabled, otherwise it falls back to local rule-based intent mapping.
- DeepPavlov endpoint should return `{ "intent": "...", "confidence": 0.0 }` and is wired by `DEEPPAVLOV_URL`.
- Log analysis events are indexed to OpenSearch when enabled.
