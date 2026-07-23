package uk.gov.hmcts.opal.config.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class RedisTtlFunctionTest {

    private Duration ttl = Duration.ofHours(8);
    private Duration hmrcTtl = Duration.ofHours(3).plusMinutes(30);
    private RedisTtlFunction ttlFunction = new RedisTtlFunction(ttl, hmrcTtl);

    @Test
    void returnsCorrectTtlDuration() {
        Duration returnedTtl = ttlFunction.getTimeToLive("SOME_KEY", null);

        assertEquals(ttl, returnedTtl);
    }

    @Test
    void returnsCorrectHmrcTtlDuration() {
        Duration returnedTtl = ttlFunction.getTimeToLive(CacheKeys.HMRC_AUTH_TOKEN, null);

        assertEquals(hmrcTtl, returnedTtl);
    }

}
