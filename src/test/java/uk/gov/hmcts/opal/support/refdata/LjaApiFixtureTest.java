package uk.gov.hmcts.opal.support.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.serenitybdd.core.Serenity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class LjaApiFixtureTest {
    private static final String JSON = """
        {"localJusticeAreaId":42,"name":"Owned court","ljaType":"LJA","addressLine1":"Court line 1",
         "addressLine2":null,"addressLine3":null,"addressLine4":null,"addressLine5":null,
         "postcode":null,"endDate":"2027-03-04T00:00:00"}
        """;

    private MockedStatic<Serenity> reporting;

    @BeforeEach
    void mockReportingBoundary() {
        reporting = mockStatic(Serenity.class, RETURNS_DEEP_STUBS);
    }

    @AfterEach
    void closeReportingBoundary() {
        reporting.close();
    }

    @Test
    void expectedIgnoresSubmittedEndDateWithoutChangingRequest() {
        var dated = LjaMessageFixture.withOverrides(LjaMessageFixture.fromFixture((short) 42, "Owned court"),
            Map.of("end_date", "2027-03-10"));
        assertThat(dated.endDate()).isEqualTo(LocalDate.of(2027, 3, 10));
        assertThat(LjaApiFixture.expected((short) 42, dated).endDate()).isNull();
    }

    @Test
    void boundsTransportAndMasksCredentialsWithoutRemovingAuthentication() {
        var request = (FilterableRequestSpecification)
            LjaApiFixture.withoutCredentialLogging(RestAssured.given()
                .header("Authorization", "Bearer fixture-only-token"));
        assertThat(request.getConfig().getLogConfig().blacklistedHeaders()).contains("Authorization");
        assertThat(request.getHeaders().getValue("Authorization")).isEqualTo("Bearer fixture-only-token");
        var params = request.getConfig().getHttpClientConfig().params();
        assertThat(params.get("http.connection.timeout")).isEqualTo(10_000);
        assertThat(params.get("http.socket.timeout")).isEqualTo(10_000);
        assertThat(params.get("http.conn-manager.timeout")).isEqualTo(10_000L);
    }

    @Test
    void acceptsGeneratedSingleRecordContractAndGenuineNulls() {
        assertThatCode(() -> fixture(200, JSON).assertRow(expected())).doesNotThrowAnyException();
    }

    @Test
    void ownershipNeedsOnlyExactBusinessIdAndTextualName() {
        var api = fixture(200, "{\"localJusticeAreaId\":42,\"name\":\"Owned court\"}");
        var marker = new LjaApiFixture.Marker((short) 42, "Owned court");
        var settings = new LjaConsumptionContext.Settings(Duration.ofMillis(100), Duration.ofMillis(1),
            Duration.ofMillis(20));

        assertThat(api.findOwned((short) 42)).contains(marker);
        assertThat(api.awaitMarker((short) 42, "Owned court", settings)).isEqualTo(marker);
        assertThatThrownBy(() -> api.assertRow(expected())).isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[]", "42", "\"text\"", "", "{broken"})
    void ownershipRequiresJsonObject(String body) {
        assertThatThrownBy(() -> fixture(200, body).findOwned((short) 42)).isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {202, 401, 500})
    void ownershipRejectsUnexpectedHttpStatus(int status) {
        assertThatThrownBy(() -> fixture(status, JSON).findOwned((short) 42)).isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"type\":\"https://hmcts.gov.uk/problems/feature-disabled\",\"status\":404}",
        "{\"type\":\"https://hmcts.gov.uk/problems/entity-not-found\",\"status\":400}",
        "{\"type\":\"https://hmcts.gov.uk/problems/entity-not-found\",\"status\":\"404\"}",
        "{\"type\":\"https://hmcts.gov.uk/problems/entity-not-found\",\"status\":404.5}"})
    void ownershipRejectsFalseEntityAbsence(String body) {
        assertThatThrownBy(() -> fixture(404, body).findOwned((short) 42)).isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @CsvSource({"42,43", "Court line 1,Wrong court", "LJA,CRWCRT", "2027-03-04,2027-03-05",
        "Owned court,Wrong name"})
    void rejectsWrongExposedFields(String value, String replacement) {
        assertThatThrownBy(() -> fixture(200, JSON.replace(value, replacement)).assertRow(expected()))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void acceptsNullEndDate() {
        var expected = expected();
        var noDate = new LjaApiFixture.Row(expected.id(), expected.name(), expected.type(),
            expected.addressLines(), expected.postcode(), null);
        assertThatCode(() -> fixture(200, JSON.replace("\"2027-03-04T00:00:00\"", "null"))
            .assertRow(noDate)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"addressLine2", "addressLine3", "addressLine4", "addressLine5", "postcode"})
    void rejectsIncorrectOptionalValues(String field) {
        assertThatThrownBy(() -> fixture(200, JSON.replace("\"" + field + "\":null",
            "\"" + field + "\":\"Wrong\"")).assertRow(expected())).isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"localJusticeAreaId", "name", "ljaType", "addressLine1", "addressLine2", "addressLine3",
        "addressLine4", "addressLine5", "postcode", "endDate"})
    void rejectsMissingExposedField(String field) throws Exception {
        var mapper = new ObjectMapper();
        var json = (ObjectNode) mapper.readTree(JSON);
        json.remove(field);
        assertThatThrownBy(() -> fixture(200, mapper.writeValueAsString(json))
            .assertRow(expected())).isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 401, 500})
    void absenceRequiresGenuine404(int status) {
        assertThatThrownBy(() -> fixture(status, JSON).assertAbsent((short) 42))
            .isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"feature-disabled", "not-found"})
    void rejects404ThatDoesNotEstablishEntityAbsence(String problemType) {
        assertThatThrownBy(() -> fixture(404, "{\"type\":\"https://hmcts.gov.uk/problems/"
            + problemType + "\",\"status\":404}").assertAbsent((short) 42)).isInstanceOf(AssertionError.class);
    }

    @Test
    void acceptsGenuine404() {
        assertThatCode(() -> fixture(404, """
            {"type":"https://hmcts.gov.uk/problems/entity-not-found","status":404}
            """).assertAbsent((short) 42)).doesNotThrowAnyException();
    }

    @Test
    void allocationDoesNotTreatFeatureDisabledAsAnAvailableKey() {
        var requests = mock(RequestSpecification.class);
        when(requests.config(any())).thenReturn(requests);
        when(requests.get(anyString())).thenReturn(new ResponseBuilder()
            .setStatusCode(404).setContentType("application/json")
            .setBody("{\"type\":\"https://hmcts.gov.uk/problems/feature-disabled\",\"status\":404}").build());
        assertThatThrownBy(() -> new LjaApiFixture("http://localhost", () -> requests)
            .allocate()).isInstanceOf(AssertionError.class);
    }

    @Test
    void allocationSkipsBothSampleIdsEvenWhenAbsent() {
        var requests = mock(RequestSpecification.class);
        when(requests.config(any())).thenReturn(requests);
        when(requests.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            int id = Integer.parseInt(url.substring(url.lastIndexOf('/') + 1));
            return new ResponseBuilder().setContentType("application/json")
                .setStatusCode(id >= 402 && id <= 405 ? 200 : 404)
                .setBody(id >= 402 && id <= 405 ? JSON.replace("42,", id + ",")
                    : "{\"type\":\"https://hmcts.gov.uk/problems/entity-not-found\",\"status\":404}").build();
        });
        var random = mock(ThreadLocalRandom.class);
        when(random.nextInt(1, 10000)).thenReturn(401);
        try (var randomness = mockStatic(ThreadLocalRandom.class)) {
            randomness.when(ThreadLocalRandom::current).thenReturn(random);
            assertThat(new LjaApiFixture("http://localhost", () -> requests).allocate()).isEqualTo((short) 407);
        }
        verify(requests, never()).get("http://localhost/local-justice-areas/401");
        verify(requests, never()).get("http://localhost/local-justice-areas/406");
    }

    @Test
    void pollingCapsTransportAndSleepToRemainingBudget() {
        var requests = mock(RequestSpecification.class);
        when(requests.config(any())).thenReturn(requests);
        when(requests.get(anyString())).thenReturn(new ResponseBuilder()
            .setStatusCode(404).setContentType("application/json")
            .setBody("{\"type\":\"https://hmcts.gov.uk/problems/entity-not-found\",\"status\":404}").build());
        var api = new LjaApiFixture("http://localhost", () -> requests);
        var settings = new LjaConsumptionContext.Settings(Duration.ofMillis(90), Duration.ofSeconds(3),
            Duration.ofMillis(90));

        assertTimeout(Duration.ofSeconds(2), () -> assertThatThrownBy(() -> api.awaitMarker((short) 42,
            "Owned court", settings)).isInstanceOf(AssertionError.class).hasMessageContaining("never observed"));

        var configs = ArgumentCaptor.forClass(RestAssuredConfig.class);
        verify(requests, atLeastOnce()).config(configs.capture());
        for (RestAssuredConfig configured : configs.getAllValues()) {
            var params = configured.getHttpClientConfig().params();
            assertThat((Integer) params.get("http.connection.timeout")).isBetween(1, 30);
            assertThat((Integer) params.get("http.socket.timeout")).isBetween(1, 30);
            assertThat((Long) params.get("http.conn-manager.timeout")).isBetween(1L, 30L);
            assertThat(configured.getLogConfig().blacklistedHeaders()).contains("Authorization");
        }
    }

    private static LjaApiFixture.Row expected() {
        return new LjaApiFixture.Row((short) 42, "Owned court", "LJA",
            Arrays.asList("Court line 1", null, null, null, null), null, LocalDate.of(2027, 3, 4));
    }

    private static LjaApiFixture fixture(int status, String body) {
        var requests = mock(RequestSpecification.class);
        when(requests.config(any())).thenReturn(requests);
        when(requests.get("http://localhost/local-justice-areas/42")).thenReturn(
            new ResponseBuilder().setStatusCode(status).setContentType("application/json").setBody(body).build());
        return new LjaApiFixture("http://localhost", () -> requests);
    }
}
