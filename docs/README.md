# Aegis Agent (MFA Support Bot)

## Objectives
- Build an intelligent L1 support agent for Authenticator Mobile and Windows application users.
- Educate users about MFA workflows, OTP generation, and approval flows.
- Automate troubleshooting, analyze logs for root causes, and escalate unresolved cases to JIRA with full context.

## What
Aegis Agent is a support layer for MFA applications that handles first-line support and log-driven diagnostics. It provides guided help, detects common failures, and escalates to engineering when resolution cannot be automated.

## Why
MFA login issues cause user friction and ticket volume. Aegis Agent reduces time-to-resolution, standardizes troubleshooting, and supplies high-quality context to engineering via JIRA.

## How
Phase 1 uses a cloud-centric model (Java + DeepPavlov) and mobile clients to capture context and logs, then performs root-cause analysis and escalates to JIRA. Phase 2 adds on-device AI for offline support and latency reduction.

## Workflow (High Level)
1. Educate user about MFA app usage and OTP/approve flows.
2. Capture user query and device metadata.
3. Analyze logs for known patterns.
4. Return guidance or escalate to JIRA with full context and log attachment.

## Phases
- Phase 1: Cloud Core (Java + DeepPavlov + Native Apps + JIRA)
- Phase 2: Hybrid AI (On-Device AI for offline and low-latency support)

## Quick Links
- Architecture: `docs/system-reminder/ARCHITECTURE.md`
- Implementation Guide: `docs/system-reminder/IMPLEMENTATION_GUIDE.md`
- Implementation Prompt: `docs/system-reminder/IMPLEMENTATION_PROMPT.md`
- Diagrams: `docs/system-reminder/DIAGRAMS.md`
