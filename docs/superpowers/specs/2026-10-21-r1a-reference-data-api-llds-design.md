# R1A and Reference Data API LLD Design

## Objective

Create standalone API-level LLD pages for every endpoint protected by the Release 1A feature toggle and for the complete Enforcer API family. Use the established `opal-api-lld` page contract and keep release ownership explicit.

## Scope

Create 20 pages under `LLD Sections/R1A` for:

1. Get Business Unit
2. Get Business Units
3. Get Court
4. Search Courts
5. Get Courts
6. Get Draft Account
7. Get Draft Accounts
8. Create Draft Account
9. Replace Draft Account
10. Update Draft Account
11. Get Local Justice Area
12. Get Local Justice Areas
13. Get Major Creditor
14. Get Major Creditors
15. Get Offence
16. Search Offences
17. Get Offences
18. Get Prosecutor
19. Get Prosecutors
20. Get Results

Create three pages under `LLD Sections/Reference Data` for:

1. Get Enforcer
2. Search Enforcers
3. Get Enforcers

The final titles must follow supplied TDIA titles where available. Where no supplied TDIA defines a title, use the operation name from the controller or OpenAPI contract. Do not include Release 1C configuration or reference-data endpoints.

## Output

Use descriptive kebab-case filenames. Every page must use this section order:

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

Do not include a Scope section in an API page. Leave the Sequence Diagram section blank. Leave the logical API specification value in API Contract empty. Leave TDIA Traceability empty when none of the supplied TDIAs applies.

`Get Results` must describe the R1A base endpoint and identify the separate R1B-only filter gate without conflating release ownership. The existing R1B page remains unchanged.

## Evidence Rules

Use repository OpenAPI and controller annotations as contract sources and the current `opal-fines-service` implementation as the source for runtime behaviour. Treat supplied TDIAs as point-in-time evidence and traceability only.

Trace each endpoint through its API interface, controller, facade or service, proxy, Opal and legacy implementations, mappers, validators, repositories, entities, views, and stored procedures as applicable. Record implementation paths relative to the `opal-fines-service` repository.

When contract, code, and TDIA evidence disagree, describe the mismatch under LLD Gaps / Questions. Do not infer undocumented stored-procedure behaviour as fact.

## Required Detail

Each page must identify:

- the exact feature-flag evaluation point and flag name, or the absence of a feature flag for Enforcer APIs;
- authentication, permission checks, and whether permission applies to any business unit or a specific business unit;
- all MapStruct, manual, inline, builder, Opal, and legacy mappers used in the flow;
- entities, tables, views, repositories, repository services, stored procedures, direct JDBC calls, and gateway actions;
- amendment tracking ownership;
- request validation, reference-data validation and resolution, and account controls;
- transaction annotations and boundaries, repository transaction semantics, locks, flushes, refreshes, and concurrency behavior;
- version checks, version increments, response version sources, and ETag behavior;
- each error scenario as a separate bullet; and
- relevant TDIA names only.

Read-only and non-account APIs must explicitly state when amendment tracking, account controls, version checks, and version increments do not apply.

## Verification

Validate all 23 pages for required headings and ordering, title sources, blank sequence diagrams, empty logical-specification values, separate error bullets, TDIA-name-only traceability, repository-relative implementation paths, and absence of machine-specific absolute paths.

Cross-check every named feature flag, permission, mapper, transaction annotation, entity, view, repository, stored procedure, validator, account control, and versioning action against the current source. Record unresolved evidence as an LLD gap.
