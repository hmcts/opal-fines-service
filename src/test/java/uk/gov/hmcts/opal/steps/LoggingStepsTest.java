package uk.gov.hmcts.opal.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;
import io.restassured.builder.ResponseBuilder;
import io.restassured.specification.RequestSpecification;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;

class LoggingStepsTest {

    private static final String LOG = """
        {"created_by":{"id":"actor","type":"OPAL_USER_ID"},
         "business_identifier":"Submit Draft Account - Defendant",
         "individuals":[{"id":"123","type":"DRAFT_ACCOUNT"}]}
        """;
    private static final String LIST_LOG = LOG.replace("Submit Draft Account", "Get Draft Account")
        .replace("{\"created_by\"", "{\"created_at\":\"2026-11-17T12:00:01Z\","
            + "\"category\":\"Consultation\",\"created_by\"");

    @AfterEach
    void clearContext() {
        ScenarioContextHolder.clear();
        Thread.interrupted();
    }

    @Test
    void resolvesLatestCreatedAccountAndUsesTableActor() {
        ScenarioContextHolder.current().addDraftAccountId("122");
        ScenarioContextHolder.current().addDraftAccountId("123");
        RequestSpecification request = requestReturning("[" + LOG + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            new LoggingSteps().loggingServiceContainsThesePdpoLogs(table("<CREATED_DRAFT_ACCOUNT_ID>", "1"));

            ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
            verify(request).body(body.capture());
            var criteria = new ObjectMapper().readTree(body.getValue());
            assertThat(criteria.path("created_by").path("id").asString()).isEqualTo("actor");
            assertThat(criteria.path("created_by").path("type").asString()).isEqualTo("OPAL_USER_ID");
            assertThat(criteria.path("individual_identifier").asString()).isEqualTo("123");
            assertThat(criteria.path("individual_type").asString()).isEqualTo("DRAFT_ACCOUNT");
            assertThat(criteria.path("business_identifier").asString())
                .isEqualTo("Submit Draft Account - Defendant");
            verify(request).post(BaseStepDef.getLoggingTestUrl() + "/test-support/search");
        }
    }

    @Test
    void waitsForExpectedCountRatherThanFirstLog() {
        RequestSpecification request = requestReturning("[" + LOG + "]");
        when(request.post(anyString())).thenReturn(
            new ResponseBuilder().setStatusCode(200).setBody("[" + LOG + "]").build(),
            new ResponseBuilder().setStatusCode(200).setBody("[" + LOG + "," + LOG + "]").build());
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            new LoggingSteps().loggingServiceContainsThesePdpoLogs(table("123", "2"));

            verify(request, times(2)).post(anyString());
        }
    }

    @Test
    void requiresAllCreatedIdsInTheSameLog() {
        ScenarioContextHolder.current().addDraftAccountId("123");
        ScenarioContextHolder.current().addDraftAccountId("456");
        String combinedLog = LOG.replace("\"individuals\":[", "\"individuals\":["
            + "{\"id\":\"456\",\"type\":\"DRAFT_ACCOUNT\"},");
        RequestSpecification request = requestReturning("[" + combinedLog + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            new LoggingSteps().loggingServiceContainsThesePdpoLogs(
                table("<CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>", "1"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"actor", "OPAL_USER_ID", "Submit Draft Account - Defendant", "123", "DRAFT_ACCOUNT"})
    void rejectsLogsWithMismatchedFields(String field) {
        RequestSpecification request = requestReturning("[" + LOG.replace(field, "wrong") + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);
            // A mismatch must retry; interrupt the retry pause to keep this test independent of the timeout.
            Thread.currentThread().interrupt();

            assertThatThrownBy(() -> new LoggingSteps().loggingServiceContainsThesePdpoLogs(table("123", "1")))
                .isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(InterruptedException.class);
        }
    }

    @Test
    void rejectsSeparateLogsForAllInOneExpectation() {
        ScenarioContextHolder.current().addDraftAccountId("123");
        ScenarioContextHolder.current().addDraftAccountId("456");
        RequestSpecification request = requestReturning("[" + LOG + "," + LOG.replace("123", "456") + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);
            Thread.currentThread().interrupt();

            assertThatThrownBy(() -> new LoggingSteps().loggingServiceContainsThesePdpoLogs(
                table("<CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>", "2")))
                .isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(InterruptedException.class);
        }
    }

    @Test
    void rejectsMissingCreatedIds() {
        assertThatThrownBy(() -> new LoggingSteps().loggingServiceContainsThesePdpoLogs(
            table("<CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>", "1")))
            .isInstanceOf(AssertionError.class).hasMessageContaining("No created draft-account IDs");
    }

    @Test
    void observesEmptySearchThroughoutTheWindow() {
        RequestSpecification request = requestReturning("[]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            new LoggingSteps().noPdpoLogsExistForCreatedBy("invalidToken", "OPAL_USER_ID", "Rejected operation");

            ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
            verify(request, atLeast(2)).body(body.capture());
            var criteria = new ObjectMapper().readTree(body.getValue());
            assertThat(criteria.size()).isEqualTo(2);
            assertThat(criteria.path("created_by").path("id").asString()).isEqualTo("invalidToken");
            assertThat(criteria.path("created_by").path("type").asString()).isEqualTo("OPAL_USER_ID");
            assertThat(criteria.path("business_identifier").asString()).isEqualTo("Rejected operation");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidToken", "PATCH007"})
    void rejectsLogsArrivingAfterAnEmptySearch(String actor) {
        RequestSpecification request = requestReturning("[]");
        when(request.post(anyString())).thenReturn(
            new ResponseBuilder().setStatusCode(200).setBody("[]").build(),
            new ResponseBuilder().setStatusCode(200).setBody("[" + LOG + "]").build());
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            assertThatThrownBy(() -> new LoggingSteps().noPdpoLogsExistForCreatedBy(
                actor, "OPAL_USER_ID", "Rejected operation"))
                .isInstanceOf(AssertionError.class).hasMessageContaining("Unexpected PDPO logs matching")
                .hasMessageContaining(actor).hasMessageContaining("Rejected operation");
            verify(request, times(2)).post(anyString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "[{}]"})
    void rejectsNonArrayOrNonEmptyAbsenceResponse(String responseBody) {
        RequestSpecification request = requestReturning(responseBody);
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            assertThatThrownBy(() -> new LoggingSteps().noPdpoLogsExistForCreatedBy(
                "invalidToken", "OPAL_USER_ID", "Rejected operation"))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Test
    void rejectsFailedSearchRatherThanTreatingItAsAbsence() {
        RequestSpecification request = requestReturning("[]");
        when(request.post(anyString())).thenReturn(
            new ResponseBuilder().setStatusCode(503).setBody("Unavailable").build());
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            assertThatThrownBy(() -> new LoggingSteps().noPdpoLogsExistForCreatedBy(
                "invalidToken", "OPAL_USER_ID", "Rejected operation"))
                .isInstanceOf(AssertionError.class).hasMessageContaining("PDPO log search failed");
        }
    }

    private RequestSpecification requestReturning(String responseBody) {
        RequestSpecification request = mock(RequestSpecification.class, RETURNS_SELF);
        when(request.post(anyString())).thenReturn(
            new ResponseBuilder().setStatusCode(200).setBody(responseBody).build());
        return request;
    }

    @Test
    void waitsForParticipantsAcrossSeparateConsultationRecords() {
        prepareListContext();
        RequestSpecification request = requestReturning("[" + LIST_LOG + "]");
        when(request.post(anyString())).thenReturn(
            new ResponseBuilder().setStatusCode(200).setBody("[" + LIST_LOG + "]").build(),
            new ResponseBuilder().setStatusCode(200)
                .setBody("[" + LIST_LOG + "," + LIST_LOG.replace("123", "456") + "]").build());
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            new LoggingSteps().loggingServiceCoversDraftAccountList(listOperations());

            ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
            verify(request, times(2)).body(body.capture());
            var criteria = new ObjectMapper().readTree(body.getValue());
            assertThat(criteria.has("individual_identifier")).isFalse();
            assertThat(criteria.path("created_after").asString()).isEqualTo("2026-11-17");
        }
    }

    @Test
    void ignoresConsultationRecordsFromEarlierListRequests() {
        prepareListContext();
        String oldLog = LIST_LOG.replace("12:00:01", "11:59:00");
        RequestSpecification request = requestReturning("[" + oldLog + "," + LIST_LOG + ","
            + LIST_LOG.replace("123", "456") + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            new LoggingSteps().loggingServiceCoversDraftAccountList(listOperations());
        }
    }

    @Test
    void rejectsDuplicateParticipantsAcrossConsultationRecords() {
        prepareListContext();
        RequestSpecification request = requestReturning("[" + LIST_LOG + "," + LIST_LOG + ","
            + LIST_LOG.replace("123", "456") + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            assertThatThrownBy(() -> new LoggingSteps().loggingServiceCoversDraftAccountList(listOperations()))
                .isInstanceOf(AssertionError.class).hasMessageContaining("Duplicate draft-account PDPO participants");
        }
    }

    @Test
    void doesNotAcceptMissingListParticipants() {
        prepareListContext();
        RequestSpecification request = requestReturning("[" + LIST_LOG + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);
            Thread.currentThread().interrupt();

            assertThatThrownBy(() -> new LoggingSteps().loggingServiceCoversDraftAccountList(listOperations()))
                .isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(InterruptedException.class);
        }
    }

    private void prepareListContext() {
        ScenarioContextHolder.current().addDraftAccountId("123");
        ScenarioContextHolder.current().addDraftAccountId("456");
        ScenarioContextHolder.current().setDraftAccountListRequestedAt(Instant.parse("2026-11-17T12:00:00Z"));
    }

    private DataTable listOperations() {
        return DataTable.create(List.of(
            List.of("created_by_id", "created_by_type", "business_identifier"),
            List.of("actor", "OPAL_USER_ID", "Get Draft Account - Defendant")),
            new DataTableTypeRegistryTableConverter(new DataTableTypeRegistry(Locale.UK)));
    }

    @Test
    void searchesCreatedAccountWithoutFilteringOutUnexpectedActors() {
        ScenarioContextHolder.current().addDraftAccountId("123");
        RequestSpecification request = requestReturning("[" + LOG + "]");
        try (MockedStatic<SerenityRest> rest = mockStatic(SerenityRest.class)) {
            rest.when(SerenityRest::given).thenReturn(request);

            assertThatThrownBy(() -> new LoggingSteps().noPdpoLogsExistForCreatedDraftAccount("Rejected operation"))
                .isInstanceOf(AssertionError.class).hasMessageContaining("Unexpected PDPO logs");

            ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
            verify(request).body(body.capture());
            var criteria = new ObjectMapper().readTree(body.getValue());
            assertThat(criteria.has("created_by")).isFalse();
            assertThat(criteria.path("individual_identifier").asString()).isEqualTo("123");
            assertThat(criteria.path("individual_type").asString()).isEqualTo("DRAFT_ACCOUNT");
            assertThat(criteria.path("business_identifier").asString()).isEqualTo("Rejected operation");
        }
    }

    private DataTable table(String individualId, String count) {
        return DataTable.create(List.of(
            List.of("created_by_id", "created_by_type", "business_identifier", "individual_id", "expected_count"),
            List.of("actor", "OPAL_USER_ID", "Submit Draft Account - Defendant", individualId, count)),
            new DataTableTypeRegistryTableConverter(new DataTableTypeRegistry(Locale.UK)));
    }
}
