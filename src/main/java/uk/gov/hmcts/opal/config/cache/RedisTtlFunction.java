package uk.gov.hmcts.opal.config.cache;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.cache.RedisCacheWriter.TtlFunction;

@RequiredArgsConstructor
public class RedisTtlFunction implements TtlFunction {

    private final Duration ttlDuration;
    private final Duration hmrcAuthTtlDuration;

    @Override
    public Duration getTimeToLive(Object key, @Nullable Object value) {
        if (key == CacheKeys.HMRC_AUTH_TOKEN) {
            return hmrcAuthTtlDuration;
        } else {
            return ttlDuration;
        }
    }
}
