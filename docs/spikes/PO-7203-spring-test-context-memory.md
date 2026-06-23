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
