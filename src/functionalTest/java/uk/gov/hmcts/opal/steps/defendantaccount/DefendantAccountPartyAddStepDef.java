package uk.gov.hmcts.opal.steps.defendantaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Functional-test steps for adding defendant-account parties.
 */
public class DefendantAccountPartyAddStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BUSINESS_UNIT_ID = "77";

    private final DefendantAccountEnforcementsActions enforcementActions = new DefendantAccountEnforcementsActions();

    private final Map<String, Integer> expectedLengths = new LinkedHashMap<>();
    private ObjectNode boundaryRequest;
    private String addedPartyId;
    private String associationId;

    /**
     * Adds a non-paying Parent/Guardian to the defendant account created in the scenario.
     */
    @When("^I add a non-paying Parent/Guardian to the created defendant account$")
    public void addNonPayingParentGuardianToCreatedDefendantAccount() {
        postParty(nonPayingParentGuardianRequest());
    }

    private void postParty(String body) {
        Response accountResponse = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        assertEquals(200, accountResponse.statusCode(), "Expected account status request to succeed");
        String etag = accountResponse.getHeader(HttpHeaders.ETAG);
        assertFalse(etag == null || etag.isBlank(), "Expected account status response ETag");

        authorisedJsonRequest()
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header(HttpHeaders.IF_MATCH, etag)
            .body(body)
            .when()
            .post(getTestUrl() + "/defendant-accounts/" + scenarioContext().getCreatedDefendantAccountIdOrFail()
                + "/defendant-account-parties");
    }

    /**
     * Asserts the add-party response retains its public shape and identifies the new party.
     *
     */
    @Then("^the add-party response identifies a non-paying Parent/Guardian with a party ID$")
    public void addPartyResponseIdentifiesNonPayingParentGuardianWithPartyId() {
        Response response = SerenityRest.lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected add-party status: " + response.getBody().asString());
        assertFalse(response.getHeader(HttpHeaders.ETAG) == null || response.getHeader(HttpHeaders.ETAG).isBlank(),
            "Expected add-party response ETag");

        JsonNode party = OBJECT_MAPPER.readTree(response.getBody().asString()).path("defendant_account_party");
        assertTrue(party.isObject(), "Expected defendant_account_party in the response");
        assertEquals("Parent/Guardian", party.path("defendant_account_party_type").asString());
        assertFalse(party.path("is_debtor").asBoolean(true), "Expected Parent/Guardian not to be a debtor");

        String partyId = party.path("party_details").path("party_id").asString();
        assertTrue(partyId != null && !partyId.isBlank(), "Expected response to contain the added party ID");
        scenarioContext().rememberPartyId("Parent/Guardian", partyId);
    }

    @When("^I add a non-paying Parent/Guardian using the maximum-length visible fixture$")
    public void addMaximumLengthGuardian(DataTable fixture) {
        boundaryRequest = (ObjectNode) OBJECT_MAPPER.readTree(nonPayingParentGuardianRequest());
        ObjectNode party = (ObjectNode) boundaryRequest.path("defendant_account_party");
        expectedLengths.clear();
        for (Map<String, String> row : fixture.asMaps()) {
            String path = row.get("path");
            String value = row.get("value");
            int length = Integer.parseInt(row.get("length"));
            assertEquals(length, value.length(), "Invalid fixture length at " + path);
            if (path.contains("telephone_number")) {
                assertTrue(value.matches("[0-9 ]+"), "Phone fixture must contain only digits and spaces");
                assertEquals(11, value.replace(" ", "").length(), "Phone fixture must contain 11 digits");
            }
            int separator = path.lastIndexOf('/');
            party.withObject(path.substring(0, separator)).put(path.substring(separator + 1), value);
            expectedLengths.put(path, length);
        }
        assertFalse(expectedLengths.isEmpty(), "Expected visible boundary values");
        postParty(boundaryRequest.toString());
    }

    @Then("^the added Parent/Guardian can be identified on the account$")
    public void identifyAddedGuardian() {
        addPartyResponseIdentifiesNonPayingParentGuardianWithPartyId();
        addedPartyId = OBJECT_MAPPER.readTree(SerenityRest.lastResponse().asString())
            .at("/defendant_account_party/party_details/party_id").asString();
        Response header = authorisedJsonRequest().get(accountUrl() + "/header-summary");
        assertEquals(200, header.statusCode(), "Account header GET failed: " + header.asString());
        associationId = header.jsonPath().getString("parent_guardian_party_id");
        assertTrue(associationId != null && !associationId.isBlank(), "Expected a guardian on the account");
    }

    @When("^I retrieve the added Parent/Guardian party details$")
    public void retrieveAddedGuardian() {
        authorisedJsonRequest().get(accountUrl() + "/defendant-account-parties/" + associationId);
    }

    @Then("^the added Parent/Guardian details preserve the visible fixture content and lengths$")
    public void assertGuardianBoundaryValues() {
        Response response = SerenityRest.lastResponse();
        assertEquals(200, response.statusCode(), "Guardian GET failed: " + response.asString());
        JsonNode party = OBJECT_MAPPER.readTree(response.asString()).path("defendant_account_party");
        assertEquals(addedPartyId, party.at("/party_details/party_id").asString(), "Expected the added guardian");
        assertEquals("Parent/Guardian", party.path("defendant_account_party_type").asString());
        assertFalse(party.path("is_debtor").asBoolean(true), "Expected a non-paying guardian");
        assertFalse(party.at("/party_details/organisation_flag").asBoolean(true), "Expected an individual guardian");
        JsonNode submitted = boundaryRequest.path("defendant_account_party");
        expectedLengths.forEach((path, length) -> {
            assertTrue(party.at(path).isString(), "Expected persisted text at " + path);
            assertEquals(submitted.at(path).asString(), party.at(path).asString(), "Changed content at " + path);
            assertEquals(length.intValue(), party.at(path).asString().length(), "Changed length at " + path);
        });
    }

    private String accountUrl() {
        return getTestUrl() + "/defendant-accounts/" + scenarioContext().getCreatedDefendantAccountIdOrFail();
    }

    private String nonPayingParentGuardianRequest() {
        return """
            {
              "defendant_account_party": {
                "defendant_account_party_type": "Parent/Guardian",
                "is_debtor": false,
                "party_details": {
                  "organisation_flag": false,
                  "individual_details": {
                    "title": "Ms",
                    "forenames": "PDPO",
                    "surname": "Functional Guardian",
                    "date_of_birth": null,
                    "age": null,
                    "national_insurance_number": null,
                    "individual_aliases": null
                  }
                },
                "address": {
                  "address_line_1": "1 Test Street",
                  "address_line_2": null,
                  "address_line_3": null,
                  "address_line_4": null,
                  "address_line_5": null,
                  "postcode": "TE1 1ST"
                },
                "contact_details": null,
                "vehicle_details": null,
                "employer_details": null,
                "language_preferences": null
              }
            }
            """;
    }
}
