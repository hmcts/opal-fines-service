# PO-7203 Spring test context memory investigation

## Summary

The integration test suite is creating many distinct full Spring Boot
`ApplicationContext` instances. Each context is heavy because the shared
integration base starts the full application with MockMvc, Flyway, Postgres,
Redis, legacy gateway wiring, LaunchDarkly, Jackson, cache infrastructure, and
the application bean graph.

The current mitigation is visible in Gradle:

- `spring.test.context.cache.maxSize=4`
- integration test JVM heap `maxHeapSize = "2g"`
- Gradle daemon heap `org.gradle.jvmargs=-Xmx2g`

Local reproduction did not hit `OutOfMemoryError`, but cache diagnostics confirm
the underlying shape: a small mixed subset produced six distinct Spring test
contexts while the cache retained only four. This supports the hypothesis that
Jenkins failures are caused by repeated creation and retention of heavy contexts,
not by a single obviously broken test class.

## Evidence

Current memory/cache settings:

- `build.gradle`: all `Test` tasks set `spring.test.context.cache.maxSize` to
  `4` and `maxHeapSize` to `2g`.
- `gradle.properties`: Gradle itself also runs with `-Xmx2g`.

Shared integration base:

- `AbstractIntegrationTest` uses full `@SpringBootTest(classes = Application.class)`.
- It adds `@AutoConfigureMockMvc`, `@ContextConfiguration(TestContainerConfig)`,
  `@Import(IntegrationSecurityConfiguration)`, and the Zephyr extension.
- `TestContainerConfig` starts Postgres and Redis Testcontainers, and can start
  the legacy stub container.
- `application-integration.yaml` keeps LaunchDarkly enabled by default.

Static context-key contributors found in `src/integrationTest/java`:

- 106 integration test classes.
- Active profile variants:
  - 31 classes with `@ActiveProfiles({"integration"})`
  - 17 classes with `@ActiveProfiles({"integration", "opal"})`
  - 16 classes with `@ActiveProfiles({"integration", "legacy"})`
  - 4 classes with `@ActiveProfiles("integration")`
  - 1 class with `@ActiveProfiles({"legacy"})`
  - 1 security profile with `inheritProfiles = false`
- 33 classes with `@TestPropertySource`.
- 18 classes with `@MockitoBean`, `@MockBean`, or `@SpyBean`.
- 1 class with `@DirtiesContext`.
- Additional per-class `@Import` customizations.

These are Spring test context cache-key inputs. In particular, inline
`@TestPropertySource` properties and `@MockitoBean` sets commonly force a new
context even when the class extends the same abstract base.

Representative examples:

- `Release1bFeatureToggleLaunchDarklyEnabledFlagFalseIntegrationTest` changes
  profiles, sets inline LaunchDarkly properties, and adds an `LDClientInterface`
  Mockito bean.
- `TestingSupportControllerIntegrationTest` adds four Mockito beans.
- `GenericReportServiceTest` adds `@DirtiesContext`, imports test beans, and adds
  two Mockito beans.
- `GlobalExceptionIntegrationTest` imports a test controller.

## Local measurement

Commands used:

```bash
JAVA_TOOL_OPTIONS='-Dlogging.level.org.springframework.test.context.cache=DEBUG -Dspring.main.banner-mode=off' \
  ./gradlew integration --no-daemon \
  --tests 'uk.gov.hmcts.opal.controllers.ReportsApiControllerIntegrationTest'
```

Result:

- Build succeeded.
- Spring context cache: `size = 1`, `maxSize = 4`, `missCount = 1`.

```bash
JAVA_TOOL_OPTIONS='-Dlogging.level.org.springframework.test.context.cache=DEBUG -Dspring.main.banner-mode=off' \
  ./gradlew integration --no-daemon \
  --tests 'uk.gov.hmcts.opal.controllers.ReportsApiControllerIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.Release1bFeatureToggleLaunchDarklyEnabledFlagFalseIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.LegacyDefendantsSearchIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.JwtControllerIntegrationTest'
```

Result:

- Build succeeded.
- Spring context cache: `size = 4`, `maxSize = 4`, `missCount = 4`.

```bash
JAVA_TOOL_OPTIONS='-Dlogging.level.org.springframework.test.context.cache=DEBUG -Dspring.main.banner-mode=off' \
  ./gradlew integration --no-daemon \
  --tests 'uk.gov.hmcts.opal.controllers.ReportsApiControllerIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.Release1bFeatureToggleLaunchDarklyEnabledFlagFalseIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.LegacyDefendantsSearchIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.JwtControllerIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.TestingSupportControllerIntegrationTest' \
  --tests 'uk.gov.hmcts.opal.controllers.OpalMajorCreditorAccountAtAGlanceIntegrationTest'
```

Result:

- Build succeeded.
- Spring context cache: `size = 4`, `maxSize = 4`, `missCount = 6`,
  `hitCount = 932`, `failureCount = 0`.
- Four LaunchDarkly clients were closed at JVM shutdown, consistent with four
  live cached contexts at the end of the run.

## Findings

1. Context caching is a contributor, but not because the cache is malfunctioning.
   It is doing what it was configured to do: retaining up to four contexts.
   The suite naturally creates more than four distinct contexts, so Jenkins will
   repeatedly build expensive contexts and evict older ones.

2. The root cause is context-key fragmentation across full `@SpringBootTest`
   integration tests. Profiles, inline property overrides, mock bean sets,
   imports, and `@DirtiesContext` multiply cache keys.

3. The contexts are heavy. A cache size of four still means four full application
   contexts can be retained at once, including LaunchDarkly clients and the full
   web/MVC infrastructure. The Jenkins failure during
   `mvcContentNegotiationManager` creation is consistent with heap exhaustion
   while another full MVC context is being built.

4. Reducing `spring.test.context.cache.maxSize` lowers retained context count,
   but it increases context rebuild churn. Increasing heap hides the pressure.
   Neither addresses the number and weight of distinct contexts.

5. Local reproduction of the exact Jenkins OOM was not achieved with the sampled
   subsets. That is expected: the local machine only ran selected classes and
   had enough memory for them. The measurable local result is cache-key churn,
   not the OOM itself.

## Recommended sustainable fix

Prioritize reducing distinct full Spring contexts before changing heap again:

1. Consolidate feature-toggle tests so they share a small number of common
   profile/property configurations instead of many per-class inline
   `@TestPropertySource` combinations.

2. Replace per-class `@MockitoBean` usage with shared test configuration where
   the same mocks are needed across multiple tests. If a test only validates a
   controller/service slice, move it out of full `@SpringBootTest`.

3. Review `@DirtiesContext` in `GenericReportServiceTest`. Keep it only if the
   test mutates singleton state that cannot be reset directly.

4. Split integration tests into more coherent Gradle tasks by context family,
   for example base integration, opal profile, legacy profile, and security.
   This reduces eviction/rebuild churn and makes memory behavior measurable per
   family.

5. Add temporary CI diagnostics while fixing:
   - keep `org.springframework.test.context.cache=DEBUG` for integration runs
     until the context count is understood;
   - add `-XX:+HeapDumpOnOutOfMemoryError` and archive heap dumps on Jenkins
     failures;
   - optionally add GC logging for the integration test JVM.

6. After reducing context count, re-test with a larger cache size such as the
   Spring default of 32 or an empirically chosen value. A low cache size should
   not be the long-term fix if it causes repeated rebuilding of the same heavy
   contexts.

## Next validation step

Run the full Jenkins-equivalent `integration` task with cache DEBUG logging and
heap-dump-on-OOM enabled. The key number to capture is the final or peak
`missCount`, because that approximates how many distinct context keys the full
suite creates in one forked test JVM.

## Investigation log

### 2026-06-24: Added diagnostics and split-task baseline

Code changes started:

- Added integration-test JVM diagnostics:
  - Spring test context cache DEBUG logs.
  - Heap dumps on OOM under `build/heap-dumps/<task>`.
  - GC/safepoint logs under `build/logs/<task>-gc.log`.
  - Diagnostic artifacts copied into `integration-output`.
- Added opt-in split tasks without changing the existing CI `integration` task:
  - `integrationBase`
  - `integrationOpal`
  - `integrationLegacy`
  - `integrationSecurity`

Local Docker/Testcontainers validation:

| Task | Classes selected | Result | Max cache size | Context misses | Hits | Failures |
| --- | ---: | --- | ---: | ---: | ---: | ---: |
| `integrationBase` | 52 | Passed | 4 | 30 | 5608 | 0 |
| `integrationOpal` | 26 | Passed | 4 | 15 | 4926 | 0 |
| `integrationLegacy` | 17 | Passed | 4 | 11 | 1301 | 0 |
| `integrationSecurity` | 3 selected / 2 XML suites | Passed | 2 | 2 | 76 | 0 |

Notes:

- The split tasks are useful for measurement and isolation, but they do not by
  themselves remove context churn. Base, Opal, and Legacy still create many
  distinct contexts inside each broad profile family.
- LaunchDarkly clients closing at JVM shutdown are a useful proxy for how many
  contexts were created over the run. The base, Opal, and Legacy runs each closed
  more clients than the cache can retain at once.
- `ReportQueueConnectivityIntegrationTest` remains excluded from the split tasks
  to match the existing `integration` task.

Current evidence for proposed fixes:

- 33 integration classes use `@TestPropertySource`.
- Many of those are feature-toggle tests with small inline property differences.
  These are strong candidates for shared composed annotations or shared test
  profiles.
- 18 concrete classes use `@MockitoBean`, `@MockBean`, `@SpyBean`, or
  `@MockitoSpyBean`; abstract superclass mock/spy declarations also affect their
  subclasses. Repeated mock customizers should be consolidated or moved to
  narrower tests where possible.
- `GenericReportServiceTest` is still the only class with `@DirtiesContext`;
  this remains a specific cleanup target.

Proposed next code fixes:

1. Create shared composed integration-test annotations for common context shapes,
   starting with feature-toggle-enabled/disabled cases.
2. Move repeated `@MockitoBean` or `@MockitoSpyBean` declarations from individual
   classes into shared test configuration where the same replacements are used.
3. Review and remove `@DirtiesContext` from `GenericReportServiceTest` if direct
   cleanup/reset is enough.
4. Consider disabling LaunchDarkly in the base integration profile by default and
   enabling it only for tests that explicitly validate LaunchDarkly behaviour.

### 2026-06-24: First proposed fixes validated

Build/test changes:

- Added `ReportQueueConnectivityIntegrationTest` exclusion to the new split
  tasks so they match the existing `integration` task.
- Removed `@DirtiesContext` from `GenericReportServiceTest`.

Validation:

- `./gradlew integration --no-daemon --tests 'uk.gov.hmcts.opal.service.GenericReportServiceTest'`
  passed.
- `./gradlew integrationBase --no-daemon` passed after the cleanup.
- Base-family cache stats after the cleanup stayed at `missCount = 30`,
  `maxSize = 4`, `hitCount = 5608`, `failureCount = 0`.

Conclusion:

- Removing `@DirtiesContext` is safe and removes one explicit context
  invalidation, but it does not materially lower context count because
  `GenericReportServiceTest` still has a unique context from `@Import` plus
  `@MockitoBean` customizers.
- A trial change to disable LaunchDarkly by default in
  `application-integration.yaml` was validated and then reverted. It did not
  reduce context misses and did not clearly reduce LaunchDarkly client shutdowns,
  so it is not currently a justified fix.
- The next high-value fix remains consolidating repeated `@TestPropertySource`
  and mock customizer combinations.

Concrete high-churn candidates:

- Base family: feature-toggle tests and controller tests with inline
  LaunchDarkly properties, plus `ReportInstancesControllerIntegrationTest`,
  `TestingSupportControllerIntegrationTest`, `CommonDraftAccountControllerIntegrationTest`,
  and `GenericReportServiceTest` due to multiple mock/import customizers.
- Opal family: `Release1bFeatureToggleLaunchDarklyEnabledFlagTrueIntegrationTest`
  and `Release1bFeatureToggleLaunchDarklyEnabledFlagFalseIntegrationTest` each
  create LaunchDarkly-enabled contexts with mocked `LDClientInterface`; several
  Opal controller tests repeat `launchdarkly.enabled=false` with only flag-value
  differences.
- Legacy family: many tests repeat `launchdarkly.enabled=false` and
  `release-1b=true`; these should be a shared profile or composed annotation
  instead of per-class inline properties.

### 2026-06-25: Removed redundant LaunchDarkly property overrides

Code changes:

- Removed redundant `@TestPropertySource` declarations from Opal tests where
  `application-opal.yaml` already provides:
  - `launchdarkly.enabled=false`
  - `launchdarkly.default-flag-values.release-1b=true`
- Removed redundant `@TestPropertySource` declarations from Legacy tests where
  `application-legacy.yaml` plus `application-integration.yaml` already provide:
  - `launchdarkly.enabled=false`
  - `launchdarkly.default-flag-values.release-1b=true`

Files changed:

- `OpalMinorCreditorIntegrationTest`
- `MinorCreditorApiControllerFeatureFlagLocalEnabledIntegrationTest`
- `OpalDefendantAccountHistoryIntegrationTest`
- `OpalMajorCreditorAccountAtAGlanceIntegrationTest`
- `OpalMajorCreditorAccountHeaderSummaryIntegrationTest`
- `LegacyMinorCreditorPatchStubIntegrationTest`
- `LegacyDefendantAccountImpositionsIntegrationTest`
- `LegacyMinorCreditorIntegrationTest`
- `LegacyMinorCreditorPatchIntegrationTest`
- `LegacyMajorCreditorAccountHeaderSummaryIntegrationTest`
- `LegacyMajorCreditorAccountAtAGlanceIntegrationTest`
- `LegacyDefendantsSearchIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationOpal` | 15 | 10 | Passed |
| `integrationLegacy` | 11 | 10 | Passed |
| `integration` | not previously captured | 55 | Passed |

Conclusion:

- Removing redundant inline properties measurably reduced context churn in the
  Opal group and modestly reduced it in the Legacy group.
- This confirms that per-class `@TestPropertySource` entries were contributing
  to distinct Spring Test context keys even when they repeated profile defaults.
- Remaining churn is now more likely from genuinely different feature-flag
  values and per-class mock/spy bean customizers.
- The full integration task now has a local Docker/Testcontainers baseline:
  `missCount = 55`, `maxSize = 4`, `hitCount = 11915`, `failureCount = 0`,
  99 XML suites, and no heap failure.

### 2026-06-25: Removed further redundant test property entries

Code changes:

- Removed remaining inline feature-flag properties where the active test profile
  already supplies the same default value.
- Collapsed duplicate `@TestPropertySource` declarations on
  `JwtControllerIntegrationTest`.

Files changed:

- `CentralFundControllerIntegrationTest`
- `ResultControllerIntegrationTest`
- `ResultControllerRelease1aDisabledIntegrationTest`
- `ResultControllerRelease1bDisabledIntegrationTest`
- `DefendantAccountSearchRelease1cDisabledIntegrationTest`
- `JwtControllerIntegrationTest`
- `OpalDefendantsSearchIntegrationTest`
- `MinorCreditorApiControllerFeatureFlagIntegrationTest`
- `DefendantAccountHistoryFeatureFlagIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationBase` | 30 | 30 | Passed |
| `integrationOpal` | 10 | 10 | Passed |
| `integrationSecurity` | 2 | 2 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- This cleanup is behaviour-safe and reduces misleading per-class configuration
  noise, but it does not further reduce context creation in the measured split
  tasks.
- The useful next fix is therefore not more default-value trimming; it should
  target remaining genuinely distinct context keys, especially tests with
  different LaunchDarkly-enabled settings or unique mock/spy bean customizers.

### 2026-06-25: Aligned LaunchDarkly-enabled Release 1B tests

Code changes:

- Removed `launchdarkly.default-flag-values.release-1b` from both
  LaunchDarkly-enabled Release 1B integration tests.
- Kept `launchdarkly.enabled=true` and `launchdarkly.sdk-key=test-sdk-key` so
  the tests still exercise the LaunchDarkly-enabled bean path.
- The flag result remains controlled by the mocked `LDClientInterface`, so the
  default fallback value is not part of the asserted behaviour.

Files changed:

- `Release1bFeatureToggleLaunchDarklyEnabledFlagFalseIntegrationTest`
- `Release1bFeatureToggleLaunchDarklyEnabledFlagTrueIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationOpal` | 10 | 9 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- This confirms that equivalent LaunchDarkly-enabled tests can share one Spring
  context when their inline properties are aligned.
- The two Release 1B LaunchDarkly-enabled tests now reuse the same context:
  both XML reports end at `missCount = 9`.

### 2026-06-25: Removed unused `AccessTokenService` mock beans

Code changes:

- Removed unused `@MockitoBean AccessTokenService` declarations from search and
  impositions integration tests.
- These mocks were not referenced by the tests; requests already use the shared
  `userStateStub` or explicit `UserStateService` stubbing.

Files changed:

- `OpalDefendantsSearchIntegrationTest`
- `LegacyDefendantsSearchIntegrationTest`
- `LegacyDefendantAccountImpositionsIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationOpal` | 9 | 9 | Passed |
| `integrationLegacy` | 10 | 10 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- The cleanup is safe and removes unnecessary bean override customizers, but it
  does not merge contexts in the measured slices.
- Remaining unique contexts are now more likely caused by still-required spies,
  gateway/user-state mock combinations, SQL setup differences, or explicit
  feature-flag properties.

### 2026-06-25: Reduced ReportInstances mock bean customizers

Code changes:

- Replaced Spring-level `@MockitoBean` declarations for `UserState` and
  `BusinessUnitUser` in `ReportInstancesControllerIntegrationTest` with plain
  test objects returned by the mocked `UserStateService`.
- Kept the Spring-level mocks that are still needed:
  - `UserStateService`
  - `ReportQueuePublisherImpl`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationBase` | 30 | 30 | Passed |

Conclusion:

- This removes unnecessary bean override customizers from one heavy base test,
  but it does not merge the test with another existing base context.
- It remains useful cleanup because mock domain objects do not need to be part
  of the Spring ApplicationContext.

### 2026-06-25: Removed non-feature LaunchDarkly disabled overrides

Code changes:

- Removed `launchdarkly.enabled=false` from tests that do not assert
  feature-toggle behaviour:
  - `CentralFundControllerIntegrationTest`
  - `ResultControllerIntegrationTest`
- These tests now use the standard integration profile LaunchDarkly setup, which
  uses the local integration flag file.

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationBase` | 30 | 29 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- This confirms that some historical `launchdarkly.enabled=false` overrides were
  unnecessary in non-feature tests and were creating avoidable context keys.
- `CentralFundControllerIntegrationTest` now reuses the next standard
  integration context; `ResultControllerIntegrationTest` still creates a unique
  context because it has a `JsonSchemaValidationService` spy.

### 2026-06-25: Removed JWT non-feature LaunchDarkly override

Code changes:

- Removed `launchdarkly.enabled=false` from `JwtControllerIntegrationTest`.
- The test's feature-disabled scenario is driven by a mocked downstream
  user-service response, not by the LaunchDarkly bean path.

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationSecurity` | 2 | 2 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- This is safe cleanup, but it does not reduce `integrationSecurity` context
  misses because `JwtControllerIntegrationTest` and `UserStateClientServiceIT`
  still require separate security contexts.

### 2026-06-25: Replaced schema validation spies with autowired services

Code changes:

- Replaced `@MockitoSpyBean JsonSchemaValidationService` with `@Autowired`
  `JsonSchemaValidationService` where the tests only call `validateOrError`
  directly and do not use Mockito spy behaviour.

Files changed:

- `ResultControllerIntegrationTest`
- `OpalDefendantsSearchIntegrationTest`
- `LegacyDefendantAccountImpositionsIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationBase` | 29 | 27 | Passed |
| `integrationOpal` | 9 | 9 | Passed |
| `integrationLegacy` | 10 | 10 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- This produced a measurable base-suite improvement by removing unnecessary spy
  bean override customizers from `ResultControllerIntegrationTest`.
- The Opal and Legacy changes are behaviour-safe cleanup but do not reduce
  misses because those classes still have other unique context-key inputs:
  feature-flag properties or gateway/user-state mock combinations.

### 2026-06-25: Removed additional base controller spy customizers

Code changes:

- Replaced direct-use `@MockitoSpyBean` dependencies with `@Autowired`
  dependencies in base controller integration tests.
- The converted dependencies are called directly by the tests and are not
  stubbed or verified through Mockito.

Files changed:

- `BusinessUnitControllerIntegrationTest`
- `CourtControllerIntegrationTest`
- `LocalJusticeAreaControllerIntegrationTest`
- `MajorCreditorControllerIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationBase` | 27 | 24 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- Removing unnecessary spy bean override customizers is now the highest-value
  fix pattern for the base suite.
- `integrationBase` has reduced from the original measured baseline of 30 misses
  to 24 misses across the LaunchDarkly and spy-cleanup changes.

### 2026-06-25: Converted further direct-use schema validation spies

Code changes:

- Replaced `@MockitoSpyBean JsonSchemaValidationService` with `@Autowired`
  where the tests only call the real validation service directly.

Files changed:

- `ProsecutorControllerIntegrationTest`
- `OffenceControllerIntegrationTest`
- `MinorCreditorControllerIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationBase` | 24 | 23 | Passed |
| `integrationOpal` | 9 | 9 | Passed |
| `integrationLegacy` | 10 | 8 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- The base suite improved by one additional context miss.
- The shared minor-creditor cleanup also reduced Legacy context churn by two
  misses while remaining neutral for Opal.

### 2026-06-25: Converted shared draft and defendant schema validators

Code changes:

- Replaced direct-use `@MockitoSpyBean JsonSchemaValidationService` with
  `@Autowired` in shared test base classes.

Files changed:

- `CommonDraftAccountControllerIntegrationTest`
- `AbstractCommonDefendantsIntegrationTest`
- `AbstractOpalDefendantsIntegrationTest`
- `AbstractLegacyDefendantsIntegrationTest`

Validation:

| Task | Before misses | After misses | Result |
| --- | ---: | ---: | --- |
| `compileIntegrationTestJava` | n/a | n/a | Passed |
| `integrationBase` | 23 | 23 | Passed |
| `integrationOpal` | 9 | 9 | Passed |
| `integrationLegacy` | 8 | 7 | Passed |
| `checkstyleIntegrationTest` | n/a | n/a | Passed |

Conclusion:

- This is safe cleanup for the draft and defendant test hierarchy.
- It reduced Legacy context churn by one additional miss, while Base and Opal
  remained unchanged because other context-key differences still dominate there.
