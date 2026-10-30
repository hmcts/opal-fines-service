# PO-7203 follow-up: integration context families (PO-8964 / PO-8968)

## Provenance and before-change review

The 30 October 2026 checkout at `06fb642ed` contained `integration` and `integrationR1A/R1B/R1C`, but none of
`integrationBase`, `integrationOpal`, `integrationLegacy`, or `integrationSecurity`. Gradle's original task
inventory confirmed this. Searches of the available checkout, local history (including changes to the task
names), sibling repositories, and matching remote branch names found no PO-7203 task implementation or spike.

Consequently the historical selectors, overlaps/gaps, cache/memory results, and diagnostic helper cannot be
verified or reused from the original source. The tasks here reconstruct the requested names and use one shared
diagnostic approach based on Spring's existing default context cache. This document is a repository follow-up,
not a recovered copy of the original spike. The original spike still needs linking/updating when available;
historical performance comparisons remain unavailable. No historical measurements have been invented.

Before implementation, the full task already selected the integration source set, excluded the manual
`ReportQueueConnectivityIntegrationTest`, refreshed the legacy stub locally, and packaged the full-suite
HTML/Zephyr reports. All `Test` tasks used JUnit Platform, cache maximum 4, worker heap 4g, full exceptions,
and `failFast=false`. The Gradle JVM separately used 2g heap and 512m maximum metaspace. Those settings and
the full-suite CI/check/JaCoCo path remain in place. No executable family existed to measure original overlap.

## Final family definitions and selection

| Task | Membership |
| --- | --- |
| `integrationBase` | Default/shared Spring contexts, non-Spring integration tests, and owning classes spanning multiple non-security modes. |
| `integrationOpal` | Explicit Opal profile or fixed non-legacy inline mode property, excluding security tests. |
| `integrationLegacy` | Explicit legacy profile or fixed legacy inline mode property, excluding security tests. |
| `integrationSecurity` | Real Spring Security profile or security foundation; takes precedence over mode. |

JUnit discovery on the compiled integration roots provides the independent executable-class inventory,
including `*IT`, nested tests, inherited tests, and custom test annotations. Spring's bootstrap metadata merges
inherited/nested profiles and inline properties without refreshing application contexts or running tests.
An explicit mode property overrides a mode profile; conflicting unresolved mode configuration fails discovery.
All nested tests remain attached to their owning executable class.

The verified inventory contains 172 classes: 88 base, 45 Opal, 29 legacy, and 10 security, plus one explicit
manual-helper exclusion. No class is duplicated or unintentionally omitted. The inventory and actual Gradle
class selectors are checked independently of test execution.

The family tasks select owning `.class` files and their nested classes from this inventory. New/renamed tests
are automatically reconsidered. `checkIntegrationContextFamilies` checks the actual task class includes/excludes
against the discovered full inventory, requires exactly the recorded family for each member, and checks the
manual exclusion in every task. There are no intentional family overlaps. Release tasks overlap by design but
are not part of the four-family partition. Disabled/conditional and extended-lane tests retain their gates.

Notable cases reviewed: Opal and legacy security subclasses belong to security; `TillsControllerLegacyModeIntegrationTest`
uses a legacy property without the legacy profile; broker/emulator tests without Spring belong to base and
remain conditional. Base membership does not imply one shared Spring cache key.

## Commands and diagnostic evidence

Use the five canonical commands in the [README](../../README.md#integration-context-families). Force fresh
measurements with `--rerun-tasks --no-build-cache --console=plain`, use the same `-PextendedTests=true` lane,
and execute the tasks sequentially. A family does not run the full suite implicitly. All failures retain normal
Gradle failure propagation and exceptions. Full-suite `integration` remains required.

Artefacts:

- `build/reports/integration-context-families/inventory.json`: discovered classes, final family, reason, profiles, exclusions.
- `build/reports/integration-context-families/<task>/summary.json`: Gradle root-suite totals, task/test runtime,
  effective lane/filter/heap/fork/cache settings, status, completeness, and per-worker observations.
- `worker-<pid>.json` in the same directory: Java version, PID, runtime heap maximum, memory arguments,
  cache hit/miss/failure/max statistics and JUnit totals. A start snapshot remains incomplete if the worker exits early.
- Standard task-specific Gradle HTML/XML results and distinct family Zephyr JSON files. Full-suite packaging is unchanged.

Full-suite diagnostics are included in `integration-output/diagnostics/` for the existing CI archive.

The service-loaded listener is present only on the integration runtime. It accesses the existing default cache
through Spring's public/protected delegate API and observes it at worker-plan boundaries; it neither adds beans
nor changes test annotations/profiles. The maximum and total context-load failure count use Spring's diagnostic
representation because not all fields have a public aggregate accessor. Unrecognised fields become `N/A`.
Final snapshots precede JVM shutdown and are not process-lifetime metrics for a custom cache implementation.

Gradle totals count passed/failed/skipped invocations; worker JUnit totals are a separate diagnostic observation,
not an extra quantity to add. Cache counters are cumulative per JVM. Heap values and memory JVM arguments
are recorded separately for workers and Gradle. Full environment values and credentials are not logged.
`testOutcomes` records stable JUnit unique IDs and outcomes for invocation callbacks. Skipping an entire
conditional container does not produce per-test callbacks; its skip remains in the Gradle totals/inventory.

## External peak RSS

For Linux with GNU time:

```sh
/usr/bin/time -v ./gradlew --no-daemon -PextendedTests=true integrationBase --rerun-tasks --no-build-cache --console=plain > /tmp/po-8964-base-linux.log 2>&1
```

For macOS:

```sh
/usr/bin/time -l ./gradlew --no-daemon -PextendedTests=true integrationBase --rerun-tasks --no-build-cache --console=plain > /tmp/po-8964-base-macos.log 2>&1
```

These capture command-level timing/memory evidence and preserve Gradle's exit status. They do not isolate the
worker or report a sum of process-tree peaks. For worker attribution on Linux, sample the reported PID with
`pidstat -r -p <pid> 1` or capture `/proc/<pid>/status` `VmHWM` before exit. Sampled RSS is a lower bound.
Record process scope, units, interval, tool, and whether Gradle/container memory is included. Normalise Linux
and macOS units. Containers have separate memory observations.

## Validation results

Validated locally on 30 October 2026 at `06fb642ed` plus the PO-8964/PO-8968 working-tree changes. Java was
21.0.2, the Gradle wrapper was 9.8.2, and the declared Spring Boot plugin was 4.1.2. Every worker used a 4 GiB
maximum heap, cache maximum 4, one fork at a time, and no per-class reforking. The rebuilt local stack supplied
the legacy gateway on port 4553; database/cache/blob fixtures used the existing Testcontainers setup.

The requested `opalDown.sh` completed with volumes preserved. `opalBuild.sh -current` completed successfully,
building the backend repositories and starting the stack without changing branches or pulling Git updates.

The following are fresh real executions with `-PextendedTests=true`, not dry-run or cached counts:

| Task | Total / passed / failed / skipped | Cache hits / misses / load failures / max | Runtime | Result |
| --- | --- | --- | --- | --- |
| `integrationBase` | 702 / 701 / 0 / 1 | 10,824 / 57 / 0 / 4 | 144.88 s | Passed |
| `integrationOpal` | 567 / 567 / 0 / 0 | 8,357 / 34 / 0 / 4 | 93.25 s | Passed |
| `integrationLegacy` | 162 / 162 / 0 / 0 | 2,370 / 26 / 0 / 4 | 65.83 s | Passed |
| `integrationSecurity` | 58 / 58 / 0 / 0 | 875 / 12 / 0 / 4 | 51.24 s | Passed |
| `integration` | 1,489 / 1,488 / 0 / 1 | 22,415 / 135 / 0 / 4 | 302.11 s | Passed |

The sole integration skip was `TillQueueBrokerTest.deliversOrderedBatchesInSeparateSessions`, gated by
`TILL_QUEUE_ASB_TEST_ENABLED`. The manual connectivity helper was intentionally excluded, not skipped.

Full and combined-family totals match exactly: 1,488 passed, zero failed, one skipped. All 1,488 executed JUnit
unique IDs and their outcomes matched, with no overlap, missing/extra invocation, or outcome difference. The
machine-readable reconciliation is `build/reports/integration-context-families/comparison.json`. All five task
summaries were complete, and the packaged full-suite diagnostics were verified against the original summary.

Further verification:

- Full unit suite: **2,541 total; 2,538 passed; zero failed; three skipped**. All 15 new helper test cases passed.
- `checkIntegrationContextFamilies`: passed for 172 classes. Test dry runs confirmed actual family/full
  selection parity in both lanes: 1,092 root test descriptors in the extended lane and 803 in the core lane.
  Parameterised execution expands these counts; dry-run counts are not executed-test counts.
- Negative control: temporarily excluding `InterfaceJobProcessedFileSummaryErrorIT` from both base and full
  caused the coverage check to reject the omission and identify the class. Another isolated fixture changing
  Opal's heap to 3g was rejected for differing JVM settings. Both returned the expected nonzero Gradle status.
- `checkstyleMain`, `checkstyleTest`, `checkstyleIntegrationTest`, and `checkstyleIntegrationTestSupport`: passed.
- `checkTestJiraAnnotations`: passed (1,196 integration methods and 385 functional cases inspected).

The security family ran with the focused helper checks, followed by this sequential invocation for the
remaining families and full suite:

```sh
/usr/bin/time -l ./gradlew -PextendedTests=true integrationBase integrationOpal integrationLegacy integration --continue --no-build-cache --console=plain
./gradlew -PextendedTests=true test checkIntegrationContextFamilies checkstyleMain checkstyleTest checkstyleIntegrationTestSupport checkstyleIntegrationTest checkTestJiraAnnotations --continue --console=plain
```

All integration tasks executed after the dry runs changed their test-execution input; none was reused as
up-to-date. For subsequent fresh measurements, use the documented `--rerun-tasks` option explicitly.

The macOS time wrapper reported 616.07 s command wall time and 117,112,832 bytes maximum RSS. This observes
the wrapper/Gradle command, not isolated test-worker RSS or Docker memory, and must not be used as the worker
peak. Test-worker peak RSS was not captured in this run; use the PID-based process above when investigating it.

The family task runtimes sum to 355.21 s, compared with 302.11 s for the full task. Family cache misses sum to
129, compared with 135 for the full task; cumulative hits sum to 22,426 versus 22,415. These are separate-JVM,
single local observations with different startup/reuse/eviction order, not a performance or memory-improvement
claim. No metric regression thresholds have been introduced.

Reconcile class/method/parameter identities as well as totals. Separate invocations have separate JVM caches
and repeat startup/container costs. The full suite may reuse contexts across family boundaries and expose
shared-state/order failures. Combined cache misses or runtime need not equal full-suite measurements.
Comparisons with a future recovered PO-7203 baseline must state differences in revision/test population,
Java/Spring/Gradle versions, cache/heap/forks, lane, host resources, logging, and cold/warm infrastructure.
The original PO-7203 baseline/spike and a fresh pre-implementation full-suite measurement are unavailable.
The verified comparison here is the final full task against the final families; historical comparison and
updating the original spike remain outstanding until that source is supplied.
