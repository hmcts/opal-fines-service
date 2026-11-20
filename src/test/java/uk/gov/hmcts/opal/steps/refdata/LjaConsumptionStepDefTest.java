package uk.gov.hmcts.opal.steps.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.Scenario;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;
import io.restassured.builder.ResponseBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.serenitybdd.core.Serenity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.gov.hmcts.opal.support.refdata.LjaApiFixture;
import uk.gov.hmcts.opal.support.refdata.LjaConsumptionContext;
import uk.gov.hmcts.opal.support.refdata.LjaMessageFixture;

class LjaConsumptionStepDefTest {
    private static final String JSON = """
        {"localJusticeAreaId":42,"name":"BASELINE","ljaType":"CRWCRT",
         "addressLine1":"New address NovaChips 171","addressLine2":"New address line 2SR1910",
         "addressLine3":"New address line 3","addressLine4":"New address line 4","addressLine5":null,
         "postcode":"NE1 2BB","endDate":null}
        """;
    private LjaConsumptionContext.State state;
    private RequestSpecification requests;
    private LjaConsumptionStepDef steps;
    private MockedStatic<Serenity> reporting;

    @BeforeEach
    void setup() {
        reporting = mockStatic(Serenity.class, RETURNS_DEEP_STUBS);
        LjaConsumptionContext.reset();
        state = LjaConsumptionContext.current();
        state.id = 42;
        state.settings = new LjaConsumptionContext.Settings(Duration.ofMillis(100), Duration.ofMillis(1),
            Duration.ofMillis(20));
        state.lastRecords = List.of(LjaMessageFixture.fromFixture((short) 42, "BASELINE"));
        state.observed = state.lastRecords.getFirst();
        requests = mock(RequestSpecification.class);
        when(requests.config(any())).thenReturn(requests);
        when(requests.body(any(Map.class))).thenReturn(requests);
        when(requests.post(anyString())).thenReturn(response(202, "{}"));
        when(requests.get(anyString())).thenReturn(response(200, JSON));
        state.api = new LjaApiFixture("http://localhost", () -> requests);
        steps = new LjaConsumptionStepDef() {
            @Override
            protected RequestSpecification authorisedJsonRequest() {
                return requests;
            }
        };
    }

    @AfterEach
    void clearContext() {
        LjaConsumptionContext.clear();
        reporting.close();
    }

    @Test
    void updateWaitsForNewMarkerBeforeAssertingAllFields() {
        steps.postLjaUpdate(table("lja_name", "<unique:updated:name>", "address_line_1", "Updated Court Address"));
        String name = state.lastRecords.getFirst().name();
        String updated = JSON.replace("BASELINE", name).replace("New address NovaChips 171", "Updated Court Address");
        when(requests.get(anyString())).thenReturn(response(200, JSON), response(200, updated), response(200, updated));

        steps.assertPublishedRecord();

        verify(requests, atLeast(4)).get(anyString());
        assertThat(state.pending).isEmpty();
        assertThat(state.observed.name()).isEqualTo(name);
        assertThat(LjaApiFixture.expected((short) 42, state.lastRecords.getFirst()))
            .isEqualTo(new LjaApiFixture.Row((short) 42, name, "CRWCRT",
                Arrays.asList("Updated Court Address", "New address line 2SR1910", "New address line 3",
                    "New address line 4", null), "NE1 2BB", null));
    }

    @Test
    void submittedEndDateIsIgnoredAfterNewMarkerAppears() throws Exception {
        steps.postLjaUpdate(table("lja_name", "<unique:dated:name>", "end_date", "2027-03-10"));
        assertThat(new ObjectMapper().writeValueAsString(state.lastMessage.body()))
            .contains("\"end_date\":\"2027-03-10\"");
        String updated = JSON.replace("BASELINE", state.lastRecords.getFirst().name());
        when(requests.get(anyString())).thenReturn(response(200, JSON), response(200, updated), response(200, updated));

        steps.assertPublishedRecord();
        steps.assertEndDateIsNull();

        assertThat(state.pending).isEmpty();
        verify(requests, atLeast(5)).get(anyString());
        assertThat(state.lastRecords.getFirst().endDate()).isEqualTo(LocalDate.of(2027, 3, 10));
        assertThat(LjaApiFixture.expected((short) 42, state.lastRecords.getFirst()).endDate()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dated", "missing"})
    void nonNullOrMissingEndDateFailsDespiteFreshMarker(String actualDate) {
        steps.postLjaUpdate(table("lja_name", "<unique:dated:name>", "end_date", "2027-03-10"));
        String updated = JSON.replace("BASELINE", state.lastRecords.getFirst().name());
        updated = "missing".equals(actualDate) ? updated.replace(",\"endDate\":null", "")
            : updated.replace("\"endDate\":null", "\"endDate\":\"2027-03-10T00:00:00\"");
        when(requests.get(anyString())).thenReturn(response(200, updated));

        assertThatThrownBy(steps::assertPublishedRecord).isInstanceOf(AssertionError.class);
        assertThat(state.pending).isEmpty();
        assertThat(state.observed).isEqualTo(state.lastRecords.getFirst());
        assertThatThrownBy(steps::assertEndDateIsNull).isInstanceOf(AssertionError.class);
        when(requests.delete(anyString())).thenAnswer(invocation -> {
            when(requests.get(anyString())).thenReturn(absent());
            return response(204, "");
        });
        new LjaConsumptionHooks() {
            @Override
            protected RequestSpecification cleanupRequest() {
                return requests;
            }
        }.afterScenario(mock(Scenario.class));
        verify(requests).delete(anyString());
        assertThat(LjaConsumptionContext.current().id).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"missingPostcode", "invalidType", "malformedDate"})
    void contractDefectStillObservesAndCleansOwnedRecord(String defect) throws Exception {
        steps.postLjaUpdate(table("lja_name", "<unique:updated:name>"));
        var json = new ObjectMapper().readTree(JSON.replace("BASELINE", state.lastRecords.getFirst().name()));
        var actual = (ObjectNode) json;
        switch (defect) {
            case "missingPostcode" -> actual.remove("postcode");
            case "invalidType" -> actual.put("ljaType", "INVALID");
            case "malformedDate" -> actual.put("endDate", "not-a-date");
            default -> throw new IllegalArgumentException(defect);
        }
        when(requests.get(anyString())).thenReturn(response(200, actual.toString()));

        assertThatThrownBy(steps::assertPublishedRecord).isInstanceOf(AssertionError.class);
        assertThat(state.pending).isEmpty();
        assertThat(state.observed).isEqualTo(state.lastRecords.getFirst());
        when(requests.delete(anyString())).thenAnswer(invocation -> {
            when(requests.get(anyString())).thenReturn(absent());
            return response(204, "");
        });
        var scenario = mock(Scenario.class);
        when(scenario.isFailed()).thenReturn(true);

        cleanup().afterScenario(scenario);

        verify(requests).delete(anyString());
        verify(scenario).log("Owned LJA 42: DELETE 204 and genuine entity-not-found 404 confirmed");
        assertThat(LjaConsumptionContext.current().id).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrongId", "missingId", "stringId", "fractionalId", "wrappedId", "hugeId",
        "wrongName", "missingName",
        "nullName", "numericName"})
    void invalidOwnershipRetainsPendingRecordWithoutDelete(String defect) throws Exception {
        state.observed = null;
        when(requests.get(anyString())).thenReturn(absent());
        steps.postLjaRequest();
        var actual = (ObjectNode) new ObjectMapper().readTree(JSON);
        switch (defect) {
            case "wrongId" -> actual.put("localJusticeAreaId", 43);
            case "missingId" -> actual.remove("localJusticeAreaId");
            case "stringId" -> actual.put("localJusticeAreaId", "42");
            case "fractionalId" -> actual.put("localJusticeAreaId", 42.5);
            case "wrappedId" -> actual.put("localJusticeAreaId", 65578);
            case "hugeId" -> actual.put("localJusticeAreaId", new BigInteger("18446744073709551658"));
            case "wrongName" -> actual.put("name", "Another publisher");
            case "missingName" -> actual.remove("name");
            case "nullName" -> actual.putNull("name");
            case "numericName" -> actual.put("name", 42);
            default -> throw new IllegalArgumentException(defect);
        }
        when(requests.get(anyString())).thenReturn(response(200, actual.toString()));

        assertThatThrownBy(steps::assertPublishedRecord).isInstanceOf(AssertionError.class);
        assertThat(state.observed).isNull();
        assertThat(state.pending).containsKey(state.lastMessage.id());
        var scenario = mock(Scenario.class);
        when(scenario.isFailed()).thenReturn(true);

        cleanup().afterScenario(scenario);

        verify(requests, never()).delete(anyString());
        assertThat(state.pending).containsKey(state.lastMessage.id());
        assertThat(LjaConsumptionContext.current().id).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"localJusticeAreaId\":43,\"name\":\"BASELINE\"}",
        "{\"name\":\"BASELINE\"}", "{\"localJusticeAreaId\":42}",
        "{\"localJusticeAreaId\":42,\"name\":\"Another publisher\"}"})
    void preDeleteRecheckRefusesInvalidOwnership(String body) {
        when(requests.get(anyString())).thenReturn(response(200, body));
        var scenario = mock(Scenario.class);
        when(scenario.isFailed()).thenReturn(true);

        cleanup().afterScenario(scenario);

        verify(requests, never()).delete(anyString());
        verify(scenario).attach(anyString(), anyString(), anyString());
        assertThat(LjaConsumptionContext.current().id).isNull();
    }

    private LjaConsumptionHooks cleanup() {
        return new LjaConsumptionHooks() {
            @Override
            protected RequestSpecification cleanupRequest() {
                return requests;
            }
        };
    }

    @Test
    void nullNameIsRejectedWithoutPersistence() throws Exception {
        state.observed = null;
        when(requests.get(anyString())).thenReturn(absent());
        when(requests.post(anyString())).thenReturn(response(400, "{}"));
        steps.postWithNullName();
        assertThat(new ObjectMapper().writeValueAsString(state.lastMessage.body())).contains("\"lja_name\":null");
        steps.assertPublishedStatus(400);
        steps.assertNoLjas("owned");
        assertThat(state.pending).isEmpty();
        verify(requests, atLeast(3)).get(anyString());
    }

    @Test
    void shortRejectionObservationKeepsNormalFiniteGetTimeout() {
        state.settings = new LjaConsumptionContext.Settings(Duration.ofMillis(5), Duration.ofSeconds(3),
            Duration.ofMillis(5));
        state.lastResponse = response(400, "{}");
        when(requests.get(anyString())).thenReturn(absent());

        steps.assertPublishedStatus(400);

        var configs = ArgumentCaptor.forClass(RestAssuredConfig.class);
        verify(requests, atLeast(1)).config(configs.capture());
        for (RestAssuredConfig configured : configs.getAllValues()) {
            var params = configured.getHttpClientConfig().params();
            assertThat(params.get("http.connection.timeout")).isEqualTo(10_000);
            assertThat(params.get("http.socket.timeout")).isEqualTo(10_000);
            assertThat(params.get("http.conn-manager.timeout")).isEqualTo(10_000L);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 401, 500})
    void rejectionObservationFailsForReturnedRowOrNon404(int status) {
        state.observed = null;
        when(requests.get(anyString())).thenReturn(absent());
        when(requests.post(anyString())).thenReturn(response(400, "{}"));
        steps.postWithNullName();
        when(requests.get(anyString())).thenReturn(absent(), response(status, JSON));
        assertThatThrownBy(() -> steps.assertPublishedStatus(400)).isInstanceOf(AssertionError.class);
    }

    @Test
    void publicationTimeoutRemainsTrackedBeforeAnyResponse() {
        state.observed = null;
        when(requests.get(anyString())).thenReturn(absent());
        when(requests.post(anyString())).thenAnswer(invocation -> {
            throw new SocketTimeoutException("Read timed out");
        });
        assertThatThrownBy(steps::postLjaRequest).isInstanceOf(SocketTimeoutException.class);
        assertThat(state.pending).containsKey(state.lastMessage.id());
    }

    @Test
    void refusesToPublishOverUnownedExistingRow() {
        state.observed = null;
        assertThatThrownBy(steps::postLjaRequest).isInstanceOf(AssertionError.class);
        assertThat(state.pending).isEmpty();
    }

    @Test
    void loadsOnlyAllocatedFixtureDefaults() {
        state.id = null;
        state.api = mock(LjaApiFixture.class);
        when(state.api.allocate()).thenReturn((short) 42);
        steps.loadNewLjaRequest();
        assertThat(state.id).isEqualTo((short) 42);
        assertThat(state.lastRecords.getFirst())
            .isEqualTo(LjaMessageFixture.fromFixture((short) 42, state.resolve("<unique:created:name>")));
    }

    private static DataTable table(String... fields) {
        var rows = new ArrayList<List<String>>();
        rows.add(List.of("field", "value"));
        for (int index = 0; index < fields.length; index += 2) {
            rows.add(List.of(fields[index], fields[index + 1]));
        }
        return DataTable.create(rows, new DataTableTypeRegistryTableConverter(new DataTableTypeRegistry(Locale.UK)));
    }

    private static Response absent() {
        return response(404, "{\"type\":\"https://hmcts.gov.uk/problems/entity-not-found\",\"status\":404}");
    }

    private static Response response(int status, String body) {
        return new ResponseBuilder().setStatusCode(status).setContentType("application/json").setBody(body).build();
    }
}
