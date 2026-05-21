---
name: opal-ticket-context
description: Read extracted OPAL TDIA markdown under `.codex-docs/tdia/.../extracted.md` and use it as pre-implementation context for a ticket. Use when a ticket already has a related TDIA folder in `.codex-docs/tdia/` and Codex needs to identify impacted frontend/backend/data areas, tests, toggles, APIs, assumptions, and NFRs before changing code.
---

# Opal Ticket Context

## Use the extracted TDIA markdown first
- Treat `.codex-docs/tdia/<tdia-name>/extracted.md` as the primary design artifact for ticket preparation.
- Prefer the TDIA folder explicitly named by the user or linked from the ticket.
- If the correct TDIA folder is not obvious, stop and ask which folder under `.codex-docs/tdia/` applies.
- If no extracted TDIA exists yet, use `$opal-ticket-tdia` first to create it.

## Build ticket context in this order
1. Read the ticket and identify the feature, scope, and likely affected journey.
2. Read the related `extracted.md`.
3. Pull the implementation-relevant TDIA sections for the ticket.
4. Inspect the codebase paths that match those sections.
5. Implement only after the ticket, TDIA, and codebase view are aligned.

## Pull the right TDIA sections
- Always read `Overview and Scope`, `Assumptions`, `Tech Decisions`, and `Non-Functional Requirements` when they exist.
- For frontend tickets, read `Opal User Portal (FE)` and its relevant child sections such as `Pages`, `Global Components`, `Services (API Only)`, `Feature Toggles`, `Design Notes`, `Payload Generation`, validators, resolvers, state store, or guards if present.
- For backend tickets, read `Opal Services (BE)` and `REST API Endpoints`.
- For database-impacting tickets, read `Opal Database (DB)`, `Libra / GOB`, and any related `Stored Procedures`, `Data`, or design notes.
- For testing work, read `Test and QA` plus `Integration / Component Tests`, `E2E / Functional Tests`, `Non-Functional Tests`, and accessibility sections.
- For flows with diagrams, read `E2E Interactions` and inspect any images referenced from `images/`.

## Convert the TDIA into implementation context
- Summarize the impacted routes, pages, APIs, services, feature toggles, data entities, and tests before touching code.
- Preserve TDIA wording and identifiers exactly when they materially affect implementation.
- Translate the TDIA into concrete repo targets: routes, components, services, validators, resolvers, state, tests, backend handlers, and DB integration points.
- Use the repo as the source of truth for current implementation details; the TDIA provides design intent, not guaranteed current state.

## Handle mismatches explicitly
- If the ticket and TDIA disagree, call out the mismatch before implementing.
- If the codebase and TDIA disagree, call out the mismatch and favor code inspection for current behavior while preserving TDIA constraints where still applicable.
- If the TDIA is broader than the ticket, constrain the implementation to the ticket scope.
- Do not invent missing approvals, waivers, SYS-NFR IDs, routes, or API behavior.

## Output expectations
- State which TDIA markdown file was used.
- Summarize the sections that informed the implementation.
- Identify the likely code areas to inspect before editing.
- Keep secrets, tokens, and PII out of notes, code, and tests.
