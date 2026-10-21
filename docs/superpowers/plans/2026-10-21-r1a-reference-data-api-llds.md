# R1A and Reference Data API LLDs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce 23 evidence-backed API LLD pages covering all R1A endpoints and all three Enforcer endpoints.

**Architecture:** Trace each API from its request contract through feature interception, controller, service/proxy, mapping, persistence or legacy gateway, then record endpoint-specific behavior using the existing Opal API LLD section contract. Keep the 20 release-owned pages in `R1A` and the non-R1A Enforcer family in `Reference Data`.

**Tech Stack:** Markdown, Spring Boot Java 21 source, Spring Data JPA, JDBC stored procedures, OpenAPI YAML, JSON Schema, supplied TDIA collateral.

## Global Constraints

- Do not add a Scope section to an API page.
- Leave `Logical API specification:` empty.
- Keep `High Level Flow` immediately after `API Contract`.
- Leave `Sequence Diagram` blank.
- Keep Concurrency and Versioning / ETag as distinct sections.
- Give every error scenario its own bullet.
- Put TDIA names only in TDIA Traceability and leave it empty when no supplied TDIA applies.
- Use paths relative to `opal-fines-service` inside the pages.
- Exclude Release 1C endpoints.

---

### Task 1: Shared R1A Reference APIs

**Files:**
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-business-unit.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-business-units.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-court.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/search-courts.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-courts.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-local-justice-area.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-local-justice-areas.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-major-creditor.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-major-creditors.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-offence.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/search-offences.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-offences.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-prosecutor.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-prosecutors.md`

- [ ] **Step 1: Trace contracts and feature interception**

Read the corresponding controllers, generated interfaces where present, `@FeatureToggle` annotations, request parameters, permissions, and declared responses. Confirm all 14 endpoints against `Release1AFeatureToggleIntegrationTest`.

- [ ] **Step 2: Trace mode routing, mapping, and persistence**

Follow each controller into facade/service, proxy, Opal and legacy implementations, mapper classes, repositories, entities/views, stored procedures, and gateway actions. Record transaction annotations and distinguish response-time reference resolution from request validation.

- [ ] **Step 3: Write the 14 pages**

Apply the exact 15-section page contract. Use operation-derived titles where no supplied TDIA title exists, empty logical-specification values, blank sequence diagrams, and one error bullet per scenario.

- [ ] **Step 4: Verify this family**

Run heading-order, absolute-path, blank-section, endpoint-method/path, and implementation-reference checks over the 14 files. Re-open every named Java symbol with `rg` before accepting it as evidence.

### Task 2: Draft Account APIs

**Files:**
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-draft-account.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-draft-accounts.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/create-draft-account.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/replace-draft-account.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/update-draft-account.md`

- [ ] **Step 1: Trace the read endpoints**

Follow the ID lookup and summary/count paths through `DraftAccountController`, service/proxy layers, mappers, repositories/entities or legacy gateway, including `restrict` handling, permissions, transaction semantics, response versions, and ETags.

- [ ] **Step 2: Trace the mutation endpoints**

Follow POST, PUT, and PATCH through JSON Schema validation, mapper layers, stored procedures or JPA writes, amendment tracking, reference validation, account controls, `If-Match`, version comparison/increment, flush/refresh, and transaction boundaries.

- [ ] **Step 3: Write the five pages**

Document each endpoint independently. Keep shared behavior accurate without claiming that PUT and PATCH have identical mutation, validation, concurrency, or versioning paths unless the code proves it.

- [ ] **Step 4: Verify this family**

Cross-check schema names, mapper methods, table/view/entity names, stored procedures, transaction annotations, version source, ETag construction, and every documented error branch against source.

### Task 3: Results Reference Data

**Files:**
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/get-results.md`

- [ ] **Step 1: Reconcile release gates**

Confirm the R1A base endpoint gate and the controller-level R1B filter gate in `ResultsApiController`, including which query parameters require R1B and which remain available in R1A.

- [ ] **Step 2: Trace and write the page**

Trace OpenAPI binding, JPA specifications, repository/entity access, `ResultMapper`, transaction semantics, and response construction. Write the page from the R1A ownership perspective while retaining the R1B boundary as an explicit runtime condition.

- [ ] **Step 3: Verify against the existing R1B page**

Ensure both documents agree on implementation facts but differ clearly in release framing; do not modify the existing R1B page.

### Task 4: Enforcer API Family

**Files:**
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/Reference Data/get-enforcer.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/Reference Data/search-enforcers.md`
- Create: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/Reference Data/get-enforcers.md`

- [ ] **Step 1: Trace all three contracts**

Read `EnforcerController` and request/response DTOs to identify path/query/body fields, authentication, permissions, validation, response codes, and whether any feature interception is present.

- [ ] **Step 2: Trace shared and endpoint-specific implementation**

Follow service/proxy mode routing, Opal and legacy implementations, mapper classes, repositories/entities/views, stored procedures, gateway actions, transaction boundaries, and reference-data resolution.

- [ ] **Step 3: Write and verify the three pages**

Write separate pages with explicit absence of the R1A feature gate where applicable. Verify that lookup, search, and list behavior are not conflated and that each documented error is source-supported.

### Task 5: Whole-Set Verification

**Files:**
- Verify: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/R1A/*.md`
- Verify: `/Users/declanclarke/Desktop/Agnetic Collatoral/Opal/LLD Sections/Reference Data/*.md`

- [ ] **Step 1: Validate inventory**

Confirm exactly 20 R1A pages and exactly three Enforcer pages, with one page per agreed endpoint and no Release 1C page.

- [ ] **Step 2: Validate structure mechanically**

Check title presence, required headings and order, absence of Scope headings, empty logical-specification values, blank Sequence Diagram bodies, error bullets, TDIA-name-only content, and absence of `/Users/` within page content.

- [ ] **Step 3: Validate evidence**

Search every page for named flags, controllers, services, proxies, mapper classes, repositories, entities, views, procedures, permissions, validators, transactions, and version operations. Confirm each against source or move uncertainty into LLD Gaps / Questions.

- [ ] **Step 4: Report completion**

Summarise the created inventory, important implementation gaps discovered, and the verification performed. Do not ask the user to review the pages.
