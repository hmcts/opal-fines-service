package uk.gov.hmcts.opal.support.refdata;

import io.restassured.response.Response;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Only scenario-owned API data and publications live here; generic cleanup runs after the LJA hook. */
public final class LjaConsumptionContext {
    private static final ThreadLocal<State> CONTEXT = ThreadLocal.withInitial(State::new);

    private LjaConsumptionContext() {
    }

    public static State current() {
        return CONTEXT.get();
    }

    public static void reset() {
        CONTEXT.set(new State());
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static final class State {
        public Short id;
        public LjaMessageFixture.RecordData observed;
        public final Map<UUID, LjaMessageFixture.RecordData> pending = new LinkedHashMap<>();
        public final String runId = UUID.randomUUID().toString().substring(0, 12);
        public List<LjaMessageFixture.RecordData> lastRecords = List.of();
        public Response lastResponse;
        public LjaMessageFixture.Message lastMessage;
        public LjaApiFixture api;
        public Settings settings;

        public String resolve(String value) {
            if (value != null && value.startsWith("<unique:") && value.endsWith(":name>")) {
                return "LJA " + runId + " " + value.substring(8, value.length() - 6);
            }
            return value;
        }

        /** Establish ownership before full-field assertions, so failed contract checks still allow cleanup. */
        public void observe(UUID messageId) {
            LjaMessageFixture.RecordData record = pending.get(messageId);
            try {
                api.awaitMarker(id, record.name(), settings);
                observed = record;
                pending.remove(messageId);
            } catch (Exception | AssertionError failure) {
                throw new AssertionError(evidence(), failure);
            }
        }

        public String evidence() {
            return "LJA ID " + id + ", publication " + (lastMessage == null ? null : lastMessage.id())
                + ", requested records " + lastRecords + ", pending UUIDs/records " + pending;
        }
    }

    public record Settings(Duration processingTimeout, Duration pollInterval, Duration observation) {
        public static Settings load() {
            return new Settings(seconds("LJA_PROCESSING_TIMEOUT_SECONDS", 660),
                seconds("LJA_POLL_INTERVAL_SECONDS", 1), seconds("LJA_OBSERVATION_SECONDS", 10));
        }

        private static Duration seconds(String name, long fallback) {
            String value = System.getenv(name);
            long seconds = value == null ? fallback : Long.parseLong(value);
            if (seconds <= 0) {
                throw new IllegalArgumentException(name + " must be positive");
            }
            return Duration.ofSeconds(seconds);
        }
    }
}
