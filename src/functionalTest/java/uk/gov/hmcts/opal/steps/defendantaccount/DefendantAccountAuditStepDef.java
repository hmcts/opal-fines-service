package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONObject;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional-test steps for end-to-end audit propagation across defendant-account write flows.
 */
public class DefendantAccountAuditStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEST_USER = "opal-test@dev.platform.hmcts.net";
    private static final String BUSINESS_UNIT_ID = "77";
    private static final String EXPECTED_BUSINESS_USER_ID = "L077JG";
    private static final String ACCOUNT_FIXTURE = "draftAccounts/accountJson/adultAccount.json";
    private static final String SUBMITTED_BY_NAME = "Laura Clerk";
    private static final String ENFORCEMENT_OVERRIDE_RESULT_ID = "FWEC";
    private static final String REPLACED_FORENAMES = "Audit Party Replace";

    private final DefendantAccountEnforcementWorkflow enforcementWorkflow = new DefendantAccountEnforcementWorkflow();
    private final DefendantAccountEnforcementsActions enforcementActions = new DefendantAccountEnforcementsActions();

    /**
     * Creates a published defendant account that can be mutated by the write endpoints under test.
     *
     * @param submittedBy value used to keep scenario setup distinct.
     * @throws Exception if the draft-account fixture cannot be published.
     */
    @Given("an auditable defendant account exists for submitted by {string}")
    public void auditableDefendantAccountExistsForSubmittedBy(String submittedBy) throws Exception {
        actAsAuditTestUser();
        enforcementWorkflow.createEnforceableDefendantAccount(auditableAccountData(submittedBy));
    }

    /**
     * Asserts the freshly published account has not yet produced amendment history rows.
     *
     * @throws Exception if the history response body cannot be parsed.
     */
    @Given("the created defendant account has no amendment history yet")
    public void createdDefendantAccountHasNoAmendmentHistoryYet() throws Exception {
        Response response = requestAmendmentHistory();
        assertEquals(200, response.statusCode(), "Expected amendment-history request to succeed");
        assertEquals(0, historyItems(response).size(), "Expected no amendment history before mutation");
    }

    /**
     * Applies an enforcement-override PATCH to the created defendant account.
     */
    @When("I patch the created defendant account with an enforcement override")
    public void patchCreatedDefendantAccountWithEnforcementOverride() throws Exception {
        Response statusResponse = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        assertEquals(200, statusResponse.statusCode(), "Expected enforcement-status request to succeed");

        JSONObject requestBody = new JSONObject()
            .put("enforcement_override", new JSONObject()
                .put("enforcement_override_result", new JSONObject()
                    .put("enforcement_override_result_id", ENFORCEMENT_OVERRIDE_RESULT_ID)));

        authorisedJsonRequest()
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", scenarioContext().getDefendantAccountEtag())
            .body(requestBody.toString())
            .when()
            .patch(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountId());
    }

    /**
     * Adds payment terms with a payment-card request for the created defendant account.
     */
    @When("I add payment terms requesting a payment card for the created defendant account")
    public void addPaymentTermsRequestingPaymentCardForCreatedDefendantAccount() {
        Response statusResponse = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        assertEquals(200, statusResponse.statusCode(), "Expected enforcement-status request to succeed");

        authorisedJsonRequest()
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", scenarioContext().getDefendantAccountEtag())
            .body(addPaymentTermsRequestBody())
            .when()
            .post(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountId() + "/payment-terms");
    }

    /**
     * Replaces the current defendant party using the existing GET response as the request base,
     * mutating only the forenames field so the PUT remains aligned with the live contract.
     *
     * @throws Exception if the current party response cannot be parsed.
     */
    @When("I replace the defendant party for the created defendant account")
    public void replaceDefendantPartyForCreatedDefendantAccount() throws Exception {
        Response headerSummary = authorisedJsonRequest()
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountId() + "/header-summary");

        assertEquals(200, headerSummary.statusCode(), "Expected header-summary request to succeed");
        String defendantAccountPartyId = headerSummary.jsonPath().getString("defendant_account_party_id");

        Response partyResponse = authorisedJsonRequest()
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountId()
                + "/defendant-account-parties/" + defendantAccountPartyId);

        assertEquals(200, partyResponse.statusCode(), "Expected defendant-account-party request to succeed");

        JsonNode root = OBJECT_MAPPER.readTree(partyResponse.getBody().asString());
        JsonNode defendantAccountParty = root.path("defendant_account_party");
        assertTrue(defendantAccountParty.isObject(), "Expected defendant_account_party object");

        ObjectNode requestParty = replacementPartyRequest((ObjectNode) defendantAccountParty);
        ensureOptionalPartySectionsPresent(requestParty);

        JsonNode individualDetails = requestParty.path("party_details").path("individual_details");
        assertTrue(individualDetails.isObject(), "Expected individual_details object on defendant party");
        ((ObjectNode) individualDetails).put("forenames", REPLACED_FORENAMES);
        String ifMatch = headerSummary.getHeader("ETag");

        authorisedJsonRequest()
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", ifMatch)
            .body(requestParty.toString())
            .when()
            .put(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountId()
                + "/defendant-account-parties/" + defendantAccountPartyId);
    }

    /**
     * Asserts the PATCH response still follows the public contract.
     *
     * @throws Exception if the response body cannot be parsed.
     */
    @Then("the defendant account patch response matches the documented contract")
    public void defendantAccountPatchResponseMatchesTheDocumentedContract() throws Exception {
        Response response = SerenityRest.lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected PATCH status");
        assertFalse(blank(response.getHeader("ETag")), "Expected PATCH response ETag");

        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(root.isObject(), "PATCH response should be a JSON object");
        assertEquals(createdDefendantAccountId(), root.path("id").asLong(), "Unexpected PATCH response id");
        assertTrue(root.path("enforcement_override").isObject(), "Expected enforcement_override object");
        assertEquals(
            ENFORCEMENT_OVERRIDE_RESULT_ID,
            root.path("enforcement_override").path("enforcement_override_result")
                .path("enforcement_override_result_id").asText(),
            "Unexpected enforcement override result id"
        );
    }

    /**
     * Asserts the payment-terms response still follows the public contract.
     *
     * @throws Exception if the response body cannot be parsed.
     */
    @Then("the payment terms response matches the documented contract")
    public void paymentTermsResponseMatchesTheDocumentedContract() throws Exception {
        Response response = SerenityRest.lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected payment-terms status: " + response.getBody().asString());

        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(root.isObject(), "Payment-terms response should be a JSON object");
        assertTrue(root.path("payment_terms").isObject(), "Expected payment_terms object");
        assertTrue(root.path("payment_card_last_requested").isTextual(), "Expected payment_card_last_requested");

        JsonNode postedDetails = root.path("payment_terms").path("posted_details");
        assertTrue(postedDetails.isObject(), "Expected payment_terms.posted_details object");
        assertEquals(EXPECTED_BUSINESS_USER_ID, postedDetails.path("posted_by").asText());
        assertEquals(TEST_USER, postedDetails.path("posted_by_name").asText());
    }

    /**
     * Asserts the replace-party response still follows the public contract.
     *
     * @throws Exception if the response body cannot be parsed.
     */
    @Then("the replace defendant account party response matches the documented contract")
    public void replaceDefendantAccountPartyResponseMatchesTheDocumentedContract() throws Exception {
        Response response = SerenityRest.lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected replace-party status: " + response.getBody().asString());
        assertFalse(blank(response.getHeader("ETag")), "Expected replace-party response ETag");

        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        JsonNode defendantAccountParty = root.path("defendant_account_party");
        assertTrue(defendantAccountParty.isObject(), "Expected defendant_account_party object");
        assertEquals(
            "Defendant",
            defendantAccountParty.path("defendant_account_party_type").asText(),
            "Unexpected defendant_account_party_type"
        );
        assertEquals(
            REPLACED_FORENAMES,
            defendantAccountParty.path("party_details").path("individual_details").path("forenames").asText(),
            "Unexpected forenames after replace"
        );
    }

    /**
     * Asserts all amendment history rows generated for the created account carry the expected
     * audit identity.
     *
     * @param expectedBusinessUserId expected posted_by value.
     * @param expectedUserName expected posted_by_name value.
     * @throws Exception if the history response body cannot be parsed.
     */
    @Then("the created defendant account amendment history is recorded for business user {string} "
        + "and username {string}")
    public void createdDefendantAccountAmendmentHistoryIsRecordedForBusinessUserAndUsername(
        String expectedBusinessUserId,
        String expectedUserName
    ) throws Exception {
        List<JsonNode> items = amendmentHistoryItems();
        if (!items.isEmpty()) {
            for (JsonNode item : items) {
                assertEquals("Amendment", item.path("type").asText(), "Expected amendment history item");
                JsonNode postedDetails = item.path("postedDetails");
                assertTrue(postedDetails.isObject(), "Expected postedDetails object");
                assertEquals(expectedBusinessUserId, postedDetails.path("posted_by").asText());
                assertEquals(expectedUserName, postedDetails.path("posted_by_name").asText());
            }
            return;
        }

        Response response = requestAmendmentsSearch();
        assertEquals(
            200,
            response.statusCode(),
            "Expected amendment-history projection or /amendments/search support endpoint to expose audit rows: "
                + response.getBody().asString()
        );

        List<JsonNode> rows = amendmentSearchRows(response);
        assertFalse(rows.isEmpty(), "Expected persisted amendments after mutation: " + response.getBody().asString());

        for (JsonNode row : rows) {
            assertEquals(expectedBusinessUserId, row.path("amended_by").asText(), "Unexpected amended_by");
            assertEquals(expectedUserName, row.path("amended_by_name").asText(), "Unexpected amended_by_name");
        }
    }

    /**
     * Asserts at least one amendment row contains the supplied new-value text.
     *
     * @param expectedNewValue expected amendment newValue.
     * @throws Exception if the history response body cannot be parsed.
     */
    @Then("the created defendant account amendment history contains new value {string}")
    public void createdDefendantAccountAmendmentHistoryContainsNewValue(String expectedNewValue) throws Exception {
        Response response = requestAmendmentHistory();
        assertEquals(200, response.statusCode(), "Expected amendment-history request to succeed");

        boolean found = historyItems(response).stream()
            .map(item -> item.path("details").path("newValue").asText())
            .anyMatch(newValue -> newValue.contains(expectedNewValue));

        if (!found) {
            Response amendmentsResponse = requestAmendmentsSearch();
            assertEquals(
                200,
                amendmentsResponse.statusCode(),
                "Expected /amendments/search support endpoint to expose audit rows: "
                    + amendmentsResponse.getBody().asString()
            );
            found = amendmentSearchRows(amendmentsResponse).stream()
                .map(row -> row.path("new_value").asText())
                .anyMatch(newValue -> newValue.contains(expectedNewValue));
        }

        assertTrue(found, "Expected amendment history to contain newValue " + expectedNewValue);
    }

    private Map<String, String> auditableAccountData(String submittedBy) {
        Map<String, String> accountData = new LinkedHashMap<>();
        accountData.put("business_unit_id", BUSINESS_UNIT_ID);
        accountData.put("account", ACCOUNT_FIXTURE);
        accountData.put("account_type", "Fine");
        accountData.put("account_status", "Submitted");
        accountData.put("submitted_by", submittedBy);
        accountData.put("submitted_by_name", SUBMITTED_BY_NAME);
        return accountData;
    }

    private String addPaymentTermsRequestBody() {
        return """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "Audit payment card request",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "ignored",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "ignored"
                }
              },
              "request_payment_card": true,
              "generate_payment_terms_change_letter": false
            }
            """;
    }

    private ObjectNode replacementPartyRequest(ObjectNode currentParty) {
        ObjectNode requestParty = OBJECT_MAPPER.createObjectNode();
        requestParty.put("defendant_account_party_type", "Defendant");
        requestParty.put("is_debtor", false);

        JsonNode currentPartyDetails = currentParty.path("party_details");
        JsonNode currentIndividualDetails = currentPartyDetails.path("individual_details");
        assertTrue(currentIndividualDetails.isObject(), "Expected current individual_details object");

        ObjectNode individualDetails = OBJECT_MAPPER.createObjectNode();
        copyIfPresent((ObjectNode) currentIndividualDetails, individualDetails, "title");
        copyIfPresent((ObjectNode) currentIndividualDetails, individualDetails, "surname");
        copyIfPresent((ObjectNode) currentIndividualDetails, individualDetails, "date_of_birth");
        copyIfPresent((ObjectNode) currentIndividualDetails, individualDetails, "national_insurance_number");

        ObjectNode partyDetails = OBJECT_MAPPER.createObjectNode();
        partyDetails.put("organisation_flag", false);
        partyDetails.set("individual_details", individualDetails);
        requestParty.set("party_details", partyDetails);

        JsonNode currentAddress = currentParty.path("address");
        if (currentAddress.isObject()) {
            requestParty.set("address", currentAddress.deepCopy());
        }

        return requestParty;
    }

    private void copyIfPresent(ObjectNode source, ObjectNode target, String fieldName) {
        JsonNode value = source.path(fieldName);
        if (!value.isMissingNode()) {
            target.set(fieldName, value.deepCopy());
        }
    }

    private void actAsAuditTestUser() {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(TEST_USER));
        scenarioContext().setCurrentUser(TEST_USER);
    }

    private long createdDefendantAccountId() {
        return Long.parseLong(scenarioContext().getCreatedDefendantAccountIdOrFail());
    }

    private void ensureOptionalPartySectionsPresent(ObjectNode requestParty) {
        ensureFieldPresent(requestParty, "contact_details");
        ensureFieldPresent(requestParty, "vehicle_details");
        ensureFieldPresent(requestParty, "employer_details");
        ensureFieldPresent(requestParty, "language_preferences");
    }

    private void ensureFieldPresent(ObjectNode node, String fieldName) {
        if (!node.has(fieldName)) {
            node.putNull(fieldName);
        }
    }

    private Response requestAmendmentHistory() {
        return given()
            .accept("*/*")
            .contentType("application/json")
            .header("Authorization", "Bearer " + BearerTokenStepDef.getToken())
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountId() + "/history?itemTypes=amendment");
    }

    private List<JsonNode> amendmentHistoryItems() throws IOException {
        Response response = requestAmendmentHistory();
        assertEquals(200, response.statusCode(), "Expected amendment-history request to succeed");
        return historyItems(response);
    }

    private Response requestAmendmentsSearch() {
        ObjectNode requestBody = OBJECT_MAPPER.createObjectNode();
        requestBody.put("associated_record_type", "defendant_accounts");
        requestBody.put("associated_record_id", String.valueOf(createdDefendantAccountId()));
        requestBody.put("business_unit_id", BUSINESS_UNIT_ID);

        return authorisedJsonRequest()
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + "/amendments/search");
    }

    private List<JsonNode> amendmentSearchRows(Response response) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        JsonNode searchData = root.path("searchData");
        assertTrue(searchData.isArray(), "Expected searchData array from /amendments/search");

        List<JsonNode> rows = new ArrayList<>();
        for (JsonNode row : searchData) {
            rows.add(row);
        }
        return rows;
    }

    private List<JsonNode> historyItems(Response response) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        JsonNode historyItems = root.path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems should be an array");
        List<JsonNode> items = new ArrayList<>();
        for (JsonNode item : historyItems) {
            items.add(item);
        }
        return items;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
