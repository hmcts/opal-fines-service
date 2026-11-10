# Legacy Action Type Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Send the eight approved canonical action type names for supported minor- and major-creditor legacy gateway requests.

**Architecture:** Preserve the existing controller, proxy, service, mapper, and gateway flow. Strengthen contract tests so expected action names are owned by tests rather than imported from production, then change only the action constants in the two legacy services.

**Tech Stack:** Java 21, Spring Boot, JUnit 5, Mockito, Gradle.

## Global Constraints

- Use exactly these canonical names: `searchMinorCreditorAccounts`, `getMinorCreditorAccount`, `getMinorCreditorAccountHeaderSummary`, `getMinorCreditorAccountAtAGlance`, `getMinorCreditorAccountHistory`, `getMajorCreditorAccountHeaderSummary`, `getMajorCreditorAccountAtAGlance`, and `getMajorCreditorAccountHistory`.
- Leave `LIBRA.get_consolidated_accounts` unchanged.
- Do not change controllers, OpenAPI definitions, request/response payloads, feature toggles, or error handling.
- Do not add compatibility aliases or fallback calls using the old action types.

---

### Task 1: Make the creditor action contract tests independent

**Files:**
- Modify: `src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorServiceTest.java`
- Modify: `src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountServiceTest.java`
- Modify: `src/integrationTest/java/uk/gov/hmcts/opal/controllers/LegacyMinorCreditorHistoryIntegrationTest.java`
- Modify: `src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMinorCreditorAtAGlanceErrorIntegrationTest.java`
- Modify: `src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMajorCreditorHeaderTest.java`
- Modify: `src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMajorCreditorHistoryTest.java`

**Interfaces:**
- Consumes: Existing four-argument `GatewayService.postToGateway(...)` calls made by the legacy services.
- Produces: Independent test expectations for all eight canonical action type strings.

- [ ] **Step 1: Add canonical test-owned constants**

Add these constants to `LegacyMinorCreditorServiceTest`:

```java
private static final String SEARCH_MINOR_CREDITOR_ACCOUNTS = "searchMinorCreditorAccounts";
private static final String GET_MINOR_CREDITOR_ACCOUNT = "getMinorCreditorAccount";
private static final String GET_MINOR_CREDITOR_ACCOUNT_HEADER_SUMMARY = "getMinorCreditorAccountHeaderSummary";
private static final String GET_MINOR_CREDITOR_ACCOUNT_AT_A_GLANCE = "getMinorCreditorAccountAtAGlance";
private static final String GET_MINOR_CREDITOR_ACCOUNT_HISTORY = "getMinorCreditorAccountHistory";
```

Add these constants to `LegacyMajorCreditorAccountServiceTest`, and remove the three static imports from `LegacyMajorCreditorAccountService`:

```java
private static final String GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY = "getMajorCreditorAccountHeaderSummary";
private static final String GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE = "getMajorCreditorAccountAtAGlance";
private static final String GET_MAJOR_CREDITOR_ACCOUNT_HISTORY = "getMajorCreditorAccountHistory";
```

- [ ] **Step 2: Assert the minor-creditor action on one successful path per operation**

Replace the first argument matcher in the successful gateway stub or verification for each operation:

```java
eq(SEARCH_MINOR_CREDITOR_ACCOUNTS)
eq(GET_MINOR_CREDITOR_ACCOUNT)
eq(GET_MINOR_CREDITOR_ACCOUNT_HEADER_SUMMARY)
eq(GET_MINOR_CREDITOR_ACCOUNT_AT_A_GLANCE)
GET_MINOR_CREDITOR_ACCOUNT_HISTORY
```

Use the corresponding constant in every history stub that currently contains `"LIBRA.get_minor_creditor_account_history"`, because those stubs must continue matching once production changes. Replace both occurrences of `"GET_MINOR_CREDITOR_ACCOUNT_PARTY"` with `GET_MINOR_CREDITOR_ACCOUNT`.

- [ ] **Step 3: Make integration expectations independent of production constants**

Use canonical local constants in the four integration-test classes:

```java
private static final String LEGACY_ACTION = "getMinorCreditorAccountHistory";
private static final String LEGACY_OPERATION = "getMinorCreditorAccountAtAGlance";
private static final String GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY =
    "getMajorCreditorAccountHeaderSummary";
private static final String GET_MAJOR_CREDITOR_ACCOUNT_HISTORY = "getMajorCreditorAccountHistory";
```

Remove the two major-creditor static imports from production. Preserve the existing WireMock/Mockito gateway assertions and change only the constant source.

- [ ] **Step 4: Run the focused unit tests and verify RED**

Run:

```bash
./gradlew test \
  --tests 'uk.gov.hmcts.opal.service.legacy.LegacyMinorCreditorServiceTest' \
  --tests 'uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountServiceTest'
```

Expected: FAIL because the production services still send one or more old `LIBRA.*`, `GET_MINOR_CREDITOR_ACCOUNT_PARTY`, or misassigned `getMinorCreditorAccount` values while the tests expect the canonical action types.

---

### Task 2: Align the production action constants

**Files:**
- Modify: `src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorService.java:53-60`
- Modify: `src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountService.java:40-45`
- Test: `src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorServiceTest.java`
- Test: `src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountServiceTest.java`

**Interfaces:**
- Consumes: The canonical string contract established by Task 1.
- Produces: Existing service methods that pass canonical action names to `GatewayService` without changing their signatures or payloads.

- [ ] **Step 1: Change the five minor-creditor action values**

Set the existing constants to:

```java
private static final String SEARCH_MINOR_CREDITORS = "searchMinorCreditorAccounts";
private static final String GET_MINOR_CREDITOR_ACCOUNT_PARTY = "getMinorCreditorAccount";
private static final String GET_MINOR_CREDITORS_ACCOUNT_AT_A_GLANCE =
    "getMinorCreditorAccountAtAGlance";
private static final String GET_MINOR_CREDITORS_ACCOUNT_HEADER_SUMMARY =
    "getMinorCreditorAccountHeaderSummary";
private static final String GET_MINOR_CREDITOR_ACCOUNT_HISTORY = "getMinorCreditorAccountHistory";
```

Do not change `UPDATE_MINOR_CREDITOR_ACCOUNT`.

- [ ] **Step 2: Change the three major-creditor action values**

Set the existing constants to:

```java
public static final String GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE =
    "getMajorCreditorAccountAtAGlance";
public static final String GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY =
    "getMajorCreditorAccountHeaderSummary";
public static final String GET_MAJOR_CREDITOR_ACCOUNT_HISTORY =
    "getMajorCreditorAccountHistory";
```

- [ ] **Step 3: Run the focused unit tests and verify GREEN**

Run:

```bash
./gradlew test \
  --tests 'uk.gov.hmcts.opal.service.legacy.LegacyMinorCreditorServiceTest' \
  --tests 'uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountServiceTest'
```

Expected: PASS with both test classes green.

- [ ] **Step 4: Run the focused integration tests**

Run:

```bash
./gradlew integration \
  --tests 'uk.gov.hmcts.opal.controllers.LegacyMinorCreditorHistoryIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.r1b.LegacyMinorCreditorAtAGlanceErrorIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.r1b.LegacyMajorCreditorHeaderTest' \
  --tests 'uk.gov.hmcts.opal.controllers.r1b.LegacyMajorCreditorHistoryTest'
```

Expected: PASS, proving the public API paths propagate the canonical action types through the legacy services.

- [ ] **Step 5: Commit the contract correction**

```bash
git add \
  src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorService.java \
  src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountService.java \
  src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorServiceTest.java \
  src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountServiceTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/LegacyMinorCreditorHistoryIntegrationTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMinorCreditorAtAGlanceErrorIntegrationTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMajorCreditorHeaderTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMajorCreditorHistoryTest.java
git commit -m "fix: align legacy creditor action types"
```

---

### Task 3: Verify the complete change

**Files:**
- Verify: `src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorService.java`
- Verify: `src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountService.java`
- Verify: all test files changed by Tasks 1 and 2.

**Interfaces:**
- Consumes: The completed production and test changes.
- Produces: Checkstyle-clean, build-verified code with no unintended action-name changes.

- [ ] **Step 1: Check for stale mismatched names in the affected code**

Run:

```bash
rg -n \
  'LIBRA\.get_minor_creditor|LIBRA\.get_major_creditor|"GET_MINOR_CREDITOR_ACCOUNT_PARTY"' \
  src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorService.java \
  src/main/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountService.java \
  src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMinorCreditorServiceTest.java \
  src/test/java/uk/gov/hmcts/opal/service/legacy/LegacyMajorCreditorAccountServiceTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/LegacyMinorCreditorHistoryIntegrationTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMinorCreditorAtAGlanceErrorIntegrationTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMajorCreditorHeaderTest.java \
  src/integrationTest/java/uk/gov/hmcts/opal/controllers/r1b/LegacyMajorCreditorHistoryTest.java
```

Expected: no matches. Separately confirm `LIBRA.get_consolidated_accounts` remains unchanged in `LegacyDefendantAccountService`.

- [ ] **Step 2: Run main-code Checkstyle**

Run: `./gradlew checkstyleMain`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run the repository-standard build**

Run: `./gradlew build`

Expected: `BUILD SUCCESSFUL` with unit tests passing.

- [ ] **Step 4: Review the final diff and repository state**

Run:

```bash
git diff --check HEAD~1..HEAD
git show --stat --oneline HEAD
git status --short --branch
```

Expected: no whitespace errors; the code commit contains only the two production services and their contract tests; the working tree is clean.
