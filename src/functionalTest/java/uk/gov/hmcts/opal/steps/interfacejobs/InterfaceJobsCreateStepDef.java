package uk.gov.hmcts.opal.steps.interfacejobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

public class InterfaceJobsCreateStepDef extends BaseStepDef {

    private static final String URL = "/interface-jobs";
    private static final String SUMMARY_URL = "/interface-jobs/summary";
    private static final short BUSINESS_UNIT_ID = 78;
    private static final String SCHEMA = SchemaPaths.POST_INTERFACE_JOBS_CREATE_RESPONSE;
    private static final String USER_WITHOUT_PERMISSION = "opal-test-2@dev.platform.hmcts.net";

    private final JsonSchemaValidationService jsonSchemaValidationService = new JsonSchemaValidationService();

    @When("I call POST \\/interface-jobs without a token")
    public void callPostInterfaceJobsWithoutAToken() {
        scenarioContext().setLatestHttpResponse(TestHttpClient.request(
            "POST",
            getTestUrl() + URL,
            Map.of(
                "Accept", "*/*",
                "Content-Type", "application/json"
            ),
            happyPathRequestBody()
        ));
    }

    @When("I call POST \\/interface-jobs as a user without permission")
    public void callPostInterfaceJobsAsUserWithoutPermission() {
        postInterfaceJobs(BearerTokenStepDef.getAccessTokenForUser(USER_WITHOUT_PERMISSION), happyPathRequestBody());
    }

    @When("I submit the interface jobs create happy path request")
    public void submitInterfaceJobsCreateHappyPathRequest() {
        postInterfaceJobs(BearerTokenStepDef.getToken(), happyPathRequestBody());
    }

    @When("I submit the interface jobs rollback request")
    public void submitInterfaceJobsRollbackRequest() {
        postInterfaceJobs(BearerTokenStepDef.getToken(), rollbackRequestBody());
    }

    @Then("the interface jobs create response matches the documented schema")
    public void interfaceJobsCreateResponseMatchesDocumentedSchema() {
        TestHttpResponse response = requireResponse();
        assertEquals(HttpStatus.OK.value(), response.statusCode(), "Unexpected HTTP status");
        jsonSchemaValidationService.validateOrError(response.body(), SCHEMA);
        assertTrue(response.body().contains("\"interface_jobs\""), "Expected interface_jobs in response");
    }

    @Then("the interface jobs rollback request leaves no partial data behind")
    public void interfaceJobsRollbackRequestLeavesNoPartialDataBehind() {
        TestHttpResponse response = requireResponse();
        assertTrue(response.statusCode() == 404 || response.statusCode() == 500 || response.statusCode() == 400,
            "Expected the rollback request to fail with a 4xx/5xx response");

        TestHttpResponse summaryResponse = getRollbackSummary();

        assertEquals(HttpStatus.OK.value(), summaryResponse.statusCode(), "Unexpected summary status");
        assertEquals("{\"interface_jobs\":[]}", summaryResponse.body().replaceAll("\\s+", ""),
            "Rollback request should not leave persisted rows behind");
    }

    private void postInterfaceJobs(String token, String body) {
        scenarioContext().setLatestHttpResponse(TestHttpClient.request(
            "POST",
            getTestUrl() + URL,
            authorisedJsonHeaders(token),
            body));
    }

    private TestHttpResponse getRollbackSummary() {
        return TestHttpClient.request(
            "GET",
            getTestUrl() + SUMMARY_URL + "?business_unit_ids=" + BUSINESS_UNIT_ID
                + "&interface_name=" + encodeQueryParameter("Rollback Interface Jobs"),
            authorisedJsonHeaders(BearerTokenStepDef.getToken()),
            null);
    }

    private String encodeQueryParameter(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Map<String, String> authorisedJsonHeaders(String token) {
        return Map.of(
            "Accept", "*/*",
            "Content-Type", "application/json",
            "Authorization", "Bearer " + token);
    }

    private TestHttpResponse requireResponse() {
        TestHttpResponse response = scenarioContext().consumeLatestHttpResponse();
        if (response == null) {
            throw new IllegalStateException("No HTTP response recorded for the current scenario");
        }
        return response;
    }

    private String happyPathRequestBody() {
        return """
               {
                 "interface_jobs": [
                   {
                     "file_name": "e2e-interface-jobs-ok.dat",
                     "source": "NATWEST",
                     "records": "[{\\"account\\":\\"abc123\\"}]",
                     "business_unit_id": %d,
                     "interface_name": "E2E Interface Jobs",
                     "created_datetime": "2026-07-14T10:00:00"
                   }
                 ]
               }
               """.formatted(BUSINESS_UNIT_ID);
    }

    private String rollbackRequestBody() {
        return """
               {
                 "interface_jobs": [
                   {
                     "file_name": "e2e-interface-jobs-1.dat",
                     "source": "NATWEST",
                     "records": "[{\\"account\\":\\"abc123\\"}]",
                     "business_unit_id": 78,
                     "interface_name": "Rollback Interface Jobs",
                     "created_datetime": "2026-07-14T10:00:00"
                   },
                   {
                     "file_name": "e2e-interface-jobs-2.dat",
                     "source": "NATWEST",
                     "records": "[{\\"account\\":\\"abc123\\"}]",
                     "business_unit_id": 99999,
                     "interface_name": "Rollback Interface Jobs",
                     "created_datetime": "2026-07-14T10:01:00"
                   }
                 ]
               }
               """.formatted(BUSINESS_UNIT_ID);
    }
}
