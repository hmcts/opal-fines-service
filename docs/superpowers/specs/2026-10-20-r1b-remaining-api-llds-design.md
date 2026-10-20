# Remaining R1B API LLD Design

## Objective

Create standalone API-level LLD pages for the focused set of non-defendant-account APIs created or changed for Release 1B. Use the established `opal-api-lld` page contract and leave the existing defendant-account LLDs unchanged.

## Scope

Create one page for each of these API titles, preserving the title used in the relevant R1B TDIA:

1. Get Central Fund
2. Get Major Creditor Account Header Summary
3. Get Major Creditor Account At A Glance
4. Get Major Creditor History
5. Get Minor Creditor Account
6. Get Minor Creditor Account Header Summary
7. Get Minor Creditor Account At A Glance
8. Get Minor Creditor Account History
9. Update Minor Creditor Account
10. Search Minor Creditor Accounts
11. Add Note
12. Get Result
13. Get Results
14. Get Mapping

Exclude unchanged Release 1A reference APIs that are merely reused by R1B journeys, including Business Units, Courts, Enforcers, Local Justice Areas, and Major Creditors reference data.

## Output

Write the pages directly under `LLD Sections/R1B` using descriptive kebab-case filenames. Do not add domain subfolders or change existing defendant-account pages.

Every page must use this section order:

1. API Contract
2. High Level Flow
3. Implementation References
4. Mappers
5. Database Interactions
6. Sequence Diagram
7. Amendment Tracking
8. Reference Data Validation
9. Account Controls
10. Legacy Mode
11. Concurrency
12. Versioning / ETag
13. Error Handling
14. TDIA Traceability
15. LLD Gaps / Questions

Do not include a Scope section in an API page. Leave the Sequence Diagram section blank. Leave the logical API specification value in API Contract empty because corresponding logical specification files are not currently present. Reference the repository OpenAPI file under Implementation References.

## Evidence Rules

Use the repository OpenAPI as the contract source and the current `opal-fines-service` implementation as the source for runtime behaviour. Use R1B TDIAs as point-in-time evidence and traceability only.

Trace each endpoint through its generated API interface, controller, facade or proxy, Opal and legacy implementations, mappers, validators, repositories, entities, views, and stored procedures as applicable. Record implementation paths relative to the `opal-fines-service` repository.

When OpenAPI, code, and TDIA evidence disagree, describe the mismatch under LLD Gaps / Questions. Do not infer undocumented stored-procedure behaviour as fact. Clearly label TDIA-only behaviour, legacy gateway delegation, and implementation inferences.

## Required Detail

Each page must identify:

- the exact point where the R1B feature flag is evaluated, including conditional R1B behaviour inside an otherwise R1A endpoint;
- permission checks and whether they apply to any business unit or a specific business unit;
- MapStruct, manual, inline, builder, Opal, and legacy mappers used in the flow;
- entities, tables, views, projections, repositories, repository services, stored procedures, and direct JDBC calls;
- amendment tracking ownership and whether it is backend-managed or stored-procedure-managed;
- reference-data validation, reference lookups, account controls, and blocked states;
- transaction annotations, transaction propagation, locks, flushes, refreshes, and other concurrency boundaries;
- version checks, version increments, forced increments, response version sources, and ETag construction;
- each error scenario as a separate bullet; and
- relevant TDIA names without paths, links, or quotations.

Read-only or non-account APIs must explicitly say when amendment tracking, account controls, version checking, or version increments do not apply.

## Verification

Before delivery, validate all 14 pages for required headings and ordering, exact API titles, empty sequence diagrams, empty logical-specification values, separate error bullets, TDIA-name-only traceability, and absence of absolute machine paths.

Cross-check every named feature flag, permission, mapper, transaction annotation, entity, view, repository, stored procedure, validator, account control, and versioning action against the current source. Report unresolved evidence as an LLD gap rather than filling it with assumptions.
