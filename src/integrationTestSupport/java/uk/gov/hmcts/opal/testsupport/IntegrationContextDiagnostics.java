package uk.gov.hmcts.opal.testsupport;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import tools.jackson.databind.json.JsonMapper;

/** Records the shared Spring cache and memory settings once per integration-test worker. */
public final class IntegrationContextDiagnostics extends SummaryGeneratingListener {

    private final Map<String, String> testOutcomes = new ConcurrentSkipListMap<>();
    private long started;

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        super.executionFinished(testIdentifier, testExecutionResult);
        if (testIdentifier.isTest()) {
            testOutcomes.put(testIdentifier.getUniqueId(), testExecutionResult.getStatus().name());
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        super.executionSkipped(testIdentifier, reason);
        if (testIdentifier.isTest()) {
            testOutcomes.put(testIdentifier.getUniqueId(), "SKIPPED");
        }
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        super.testPlanExecutionStarted(testPlan);
        started = System.nanoTime();
        writeSnapshot(false);
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        super.testPlanExecutionFinished(testPlan);
        writeSnapshot(true);
    }

    private void writeSnapshot(boolean complete) {
        String directory = System.getProperty("opal.integration.diagnostics.directory");
        if (directory == null) {
            return;
        }
        long pid = ProcessHandle.current().pid();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("workerPid", pid);
        report.put("observedAt", Instant.now().toString());
        report.put("complete", complete);
        report.put("javaVersion", System.getProperty("java.version"));
        report.put("maxHeapBytes", Runtime.getRuntime().maxMemory());
        report.put("memoryArguments", ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
            .filter(argument -> argument.startsWith("-Xmx") || argument.startsWith("-Xms")
                || argument.startsWith("-XX:MaxMetaspaceSize=")).toList());
        report.put("runtimeSeconds", (System.nanoTime() - started) / 1_000_000_000.0);
        report.put("cache", new CacheAccess().snapshot());
        if (complete) {
            var summary = getSummary();
            report.put("tests", Map.of("total", summary.getTestsFoundCount(),
                "passed", summary.getTestsSucceededCount(), "failed", summary.getTestsFailedCount(),
                "skipped", summary.getTestsSkippedCount() + summary.getTestsAbortedCount()));
            report.put("containerFailures", summary.getContainersFailedCount());
            report.put("testOutcomes", testOutcomes);
            System.out.printf("Integration worker %d: Spring context cache %s; max heap %d bytes%n",
                pid, report.get("cache"), report.get("maxHeapBytes"));
        }
        try {
            Files.createDirectories(Path.of(directory));
            JsonMapper.builder().build().writerWithDefaultPrettyPrinter()
                .writeValue(Path.of(directory, "worker-" + pid + ".json").toFile(), report);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot write integration diagnostics to " + directory, exception);
        }
    }

    static Map<String, Object> cacheSnapshot(ContextCache cache) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("size", cache.size());
        result.put("hitCount", cache.getHitCount());
        result.put("missCount", cache.getMissCount());
        // Spring exposes total load failures and the configured maximum in its diagnostic representation.
        for (String field : new String[] {"failureCount", "maxSize"}) {
            Matcher matcher = Pattern.compile("\\b" + field + " = (\\d+)").matcher(cache.toString());
            result.put(field, matcher.find() ? Integer.parseInt(matcher.group(1)) : "N/A");
        }
        return result;
    }

    private static final class CacheAccess extends DefaultCacheAwareContextLoaderDelegate {

        Map<String, Object> snapshot() {
            return cacheSnapshot(getContextCache());
        }
    }
}
