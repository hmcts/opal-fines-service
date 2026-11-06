package uk.gov.hmcts.opal.testsupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultContextCache;
import org.springframework.test.context.support.DelegatingSmartContextLoader;

class IntegrationContextDiagnosticsTest {

    @Test
    void reportsActualCacheCountersAndMaximum() {
        var cache = new DefaultContextCache(7);
        var snapshot = IntegrationContextDiagnostics.cacheSnapshot(cache);
        assertThat(snapshot).containsEntry("size", 0).containsEntry("hitCount", 0)
            .containsEntry("missCount", 0).containsEntry("failureCount", 0).containsEntry("maxSize", 7);
    }

    @Test
    void unavailableStatisticsAreNotReportedAsZero() {
        var snapshot = IntegrationContextDiagnostics.cacheSnapshot(mock(ContextCache.class));
        assertThat(snapshot).containsEntry("failureCount", "N/A").containsEntry("maxSize", "N/A");
    }

    @Test
    void cacheMissesHitsAndLoadFailuresRemainDistinct() {
        var key = new MergedContextConfiguration(getClass(), null, null, null,
            new DelegatingSmartContextLoader());
        var cache = new DefaultContextCache(4);
        assertThat(cache.get(key)).isNull();
        try (var context = new GenericApplicationContext()) {
            context.refresh();
            cache.put(key, context);
            assertThat(cache.get(key)).isSameAs(context);
            cache.incrementFailureCount(key);
            assertThat(IntegrationContextDiagnostics.cacheSnapshot(cache))
                .containsEntry("hitCount", 1).containsEntry("missCount", 1).containsEntry("failureCount", 1);
        }
    }
}
