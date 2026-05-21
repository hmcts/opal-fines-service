---
name: opal-ticket-tdia
description: Read OPAL TDIA exports and extract implementation context before planning, implementing, or reviewing a ticket. Use when a ticket has a saved Confluence HTML view-source TDIA and Codex needs scope, impacted frontend/backend/data areas, testing expectations, assumptions, tech decisions, or NFRs before touching code.
---

# Opal Ticket TDIA

## Use HTML as the primary source
- Treat the TDIA as an input constraint, not as proof that the current code matches the design.
- Prefer a local saved Confluence HTML file supplied by the user or stored under `.codex-docs/tdia/`.
- Preferred artifact: the browser-saved Confluence “View Source” page (`.html`) together with its companion `*_files/` folder, because that preserves headings, lists, links, tables, and embedded images.
- If the ticket only includes a Confluence URL, stop and ask for a saved HTML export.
- If the TDIA includes an `E2E Interactions` diagram, check the saved page’s companion `*_files/` folder first. Only ask for a separate exported image if the saved page assets do not contain it.
- Read the extracted markdown, not the raw source artifact, whenever possible to keep context lean.
- Default to a full extraction so no design detail is silently skipped. Narrow with `--section` only when the user clearly wants a subset.

## Extract the TDIA before coding
- Run `python3 scripts/extract_tdia.py <source-path>` first.
- If the TDIA will be reused, prefer `--output-root .codex-docs/tdia` so the script creates `.codex-docs/tdia/<tdia-name>/source.html`, `.codex-docs/tdia/<tdia-name>/extracted.md`, and `images/` beside them when embedded images are found.
- The script accepts `.html` and `.htm`.
- By default the script extracts the full TDIA, including the document preamble/title block and all discovered headings.
- The script automatically detects embedded images from the saved page’s sibling `*_files/` folder and copies `E2E Interactions` images into `images/`, then references them from the `E2E Interactions` section in `extracted.md`.
- If the saved page assets do not contain the E2E diagram, pass `--e2e-image <image-path>` to supply it explicitly.
- If you only need a few sections, pass `--section` multiple times to narrow the output.

Example:

```bash
python3 .codex/skills/opal-fines-service/opal-ticket-tdia/scripts/extract_tdia.py \
  '/Users/maxholland/Downloads/View Source.html' \
  --output-root .codex-docs/tdia \
  --e2e-image '/Users/maxholland/Downloads/e2e-interactions.png'
```

## Pull implementation-relevant sections
- After extracting the full TDIA, always read `Overview and Scope`, `Assumptions`, and `Tech Decisions` when they exist.
- For frontend tickets, read `Opal User Portal (FE)` and any child sections such as `Pages`, `Global Components`, `Services (API Only)`, `Feature Toggles`, or feature-specific notes.
- Read backend or data sections only when the ticket touches them: `Opal Services (BE)`, `REST API Endpoints`, `Opal Database (DB)`, `Azure Infrastructure`, `Libra / GOB`, or `ETL`.
- Read the full testing section that applies to the ticket: `Integration / Component Tests`, `Frontend tests`, `Backend tests`, `E2E / Functional Tests`, `Non-Functional Tests`, `Automated Accessibility Tests`, `Manual Accessibility Tests`, and `Release-based Testing`.
- Read `Non-Functional Requirements`, `Response Time Targets`, `Specific NFRs`, and related NFR subsections whenever behavior, performance, accessibility, privacy, or resilience could be affected.

## Implement against the codebase, not only the document
- After extracting the TDIA, inspect the existing code paths, routes, services, tests, and toggles in the repo.
- Verify that the ticket still matches the current implementation and highlight any mismatch between the ticket, TDIA, and codebase.
- Preserve TDIA terms and IDs exactly when quoting or summarizing them.
- Do not invent missing requirements, waivers, approvals, or SYS-NFR identifiers.

## Handle extraction limits explicitly
- If the HTML extraction contains Confluence macro placeholders or saved-page noise, say so and call out the affected sections.
- If the `E2E Interactions` section refers to a diagram but no image was provided, ask for the exported image and keep a note in `extracted.md` until it is added.
- If a section expected by the ticket is missing from the TDIA, continue with the codebase evidence but call out the gap.
- If a section is duplicated because of a table of contents or repeated layout artifacts, rely on the extracted section body with the most substantive content.

## Output expectations
- Summarize the TDIA sections used before or alongside implementation work.
- Include the TDIA source path in the final summary when it materially influenced the change.
- Keep secrets, tokens, and PII out of extracted notes, code, and tests.
