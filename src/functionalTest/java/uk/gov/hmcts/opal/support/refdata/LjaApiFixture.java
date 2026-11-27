package uk.gov.hmcts.opal.support.refdata;

import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import net.serenitybdd.core.Serenity;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;

/** Reads the production single-record contract; reports contain only expected and actual response data. */
public class LjaApiFixture {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private final String baseUrl;
    private final Supplier<RequestSpecification> requests;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public LjaApiFixture(String baseUrl, Supplier<RequestSpecification> requests) {
        this.baseUrl = baseUrl;
        this.requests = requests;
    }

    public record Row(short id, String name, String type, List<String> addressLines, String postcode,
                      LocalDate endDate) {
    }

    public record Marker(short id, String name) {
    }

    public static Row expected(short id, LjaMessageFixture.RecordData record) {
        var court = record.addresses().stream().filter(address -> "Court Address".equals(address.type()))
            .findFirst().orElseThrow();
        return new Row(id, record.name(), "PSA".equals(record.type()) ? "LJA" : record.type(), court.lines(),
            court.postcode(), null);
    }

    public void assertRow(Row expected) {
        assertThat(find(expected.id())).as("Production GET must expose every expected LJA field")
            .contains(expected);
    }

    public Optional<Row> find(short id) {
        return read(id, REQUEST_TIMEOUT).map(this::fullRow);
    }

    private Row fullRow(JsonNode json) {
        List.of("localJusticeAreaId", "name", "ljaType", "addressLine1", "addressLine2", "addressLine3",
            "addressLine4", "addressLine5", "postcode", "endDate")
            .forEach(field -> assertThat(json.has(field)).as("Exposed field %s", field).isTrue());
        try {
            LocalJusticeArea actual = mapper.treeToValue(json, LocalJusticeArea.class);
            return new Row(actual.getLocalJusticeAreaId(), actual.getName(),
                actual.getLjaType().getValue(), Arrays.asList(actual.getAddressLine1(), actual.getAddressLine2(),
                    actual.getAddressLine3(), actual.getAddressLine4(), actual.getAddressLine5()), actual.getPostcode(),
                actual.getEndDate() == null ? null : actual.getEndDate().toLocalDate());
        } catch (JsonProcessingException exception) {
            throw new AssertionError("Cannot parse production single-LJA response", exception);
        }
    }

    private Optional<JsonNode> read(short id, Duration remaining) {
        Response response = withoutCredentialLogging(requests.get(), remaining)
            .get(baseUrl + "/local-justice-areas/" + id);
        evidence(id, "Production single-LJA response", response);
        assertThat(response.statusCode()).as("Production GET for LJA %s: %s", id, response.asString())
            .isIn(200, 404);
        try {
            var json = mapper.readTree(response.asString());
            assertThat(json != null && json.isObject()).as("Single-LJA response must be an object").isTrue();
            if (response.statusCode() == 404) {
                assertThat(json.path("type").asText()).as("Entity absence, not a disabled feature or unknown route")
                    .isEqualTo("https://hmcts.gov.uk/problems/entity-not-found");
                assertThat(json.path("status").isIntegralNumber()).as("Numeric problem status").isTrue();
                assertThat(json.path("status").bigIntegerValue()).isEqualTo(BigInteger.valueOf(404));
                return Optional.empty();
            }
            assertThat(json.path("localJusticeAreaId").isIntegralNumber()).as("Numeric business key").isTrue();
            assertThat(json.path("localJusticeAreaId").bigIntegerValue()).as("Requested business key")
                .isEqualTo(BigInteger.valueOf(id));
            assertThat(json.path("name").isTextual()).as("Textual publication name").isTrue();
            return Optional.of(json);
        } catch (JsonProcessingException exception) {
            throw new AssertionError("Cannot parse production single-LJA response", exception);
        }
    }

    /** Identify owned records without deserializing unrelated response-contract fields. */
    public Optional<Marker> findOwned(short id) {
        return findOwned(id, REQUEST_TIMEOUT);
    }

    private Optional<Marker> findOwned(short id, Duration remaining) {
        return read(id, remaining).map(json -> new Marker(id, json.get("name").textValue()));
    }

    public void assertAbsent(short id) {
        assertAbsent(id, REQUEST_TIMEOUT);
    }

    public void assertAbsent(short id, Duration remaining) {
        assertThat(read(id, remaining)).as("Production GET absence for LJA %s", id).isEmpty();
    }

    public short allocate() {
        int limit = 10000;
        int start = ThreadLocalRandom.current().nextInt(1, limit);
        for (int offset = 0; offset < limit - 1; offset++) {
            short id = (short) (1 + (start - 1 + offset) % (limit - 1));
            // The user's example is never published, even if it happens to be absent.
            if (id != 401 && id != 406 && find(id).isEmpty()) {
                return id;
            }
        }
        throw new IllegalStateException("No absent four-digit LJA key available; use serial functional execution");
    }

    public Marker awaitMarker(short id, String name, LjaConsumptionContext.Settings settings) {
        long deadline = System.nanoTime() + settings.processingTimeout().toNanos();
        Optional<Marker> actual = Optional.empty();
        long remaining = deadline - System.nanoTime();
        while (remaining > 0) {
            actual = findOwned(id, Duration.ofNanos(remaining));
            if (actual.isPresent() && name != null && name.equals(actual.get().name())) {
                return actual.get();
            }
            remaining = deadline - System.nanoTime();
            if (remaining > 0) {
                pause(Duration.ofNanos(Math.min(settings.pollInterval().toNanos(), remaining)));
            }
            remaining = deadline - System.nanoTime();
        }
        throw new AssertionError("LJA publication marker never observed for ID " + id + ", name " + name
            + "; last GET " + actual);
    }

    public static void pause(Duration interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling LJA production API", exception);
        }
    }

    public static RequestSpecification withoutCredentialLogging(RequestSpecification request) {
        return withoutCredentialLogging(request, REQUEST_TIMEOUT);
    }

    private static RequestSpecification withoutCredentialLogging(RequestSpecification request, Duration remaining) {
        // Apache's lease/connect/read timeouts are separate. Share the remaining budget between them;
        // zero means unbounded, so keep a one-millisecond floor even at the end of a poll window.
        int timeoutMillis = (int) Math.max(1, Math.min(10_000, remaining.toMillis() / 3));
        return request.config(config().logConfig(logConfig().blacklistHeader("Authorization"))
            .httpClient(httpClientConfig().setParam("http.connection.timeout", timeoutMillis)
                .setParam("http.socket.timeout", timeoutMillis)
                .setParam("http.conn-manager.timeout", (long) timeoutMillis)));
    }

    private static void evidence(short id, String expected, Response response) {
        Serenity.recordReportData().withTitle("LJA " + id + " production API expected/actual")
            .andContents("Expected: " + expected + "\nActual HTTP " + response.statusCode()
                + ": " + response.asString());
    }
}
