package uk.gov.hmcts.opal.steps.defendantaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.response.Response;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Functional-test steps for adding defendant-account parties.
 */
public class DefendantAccountPartyAddStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BUSINESS_UNIT_ID = "77";

    private final DefendantAccountEnforcementsActions enforcementActions = new DefendantAccountEnforcementsActions();

    /**
     * Adds a non-paying Parent/Guardian to the defendant account created in the scenario.
     */
    @When("^I add a non-paying Parent/Guardian to the created defendant account$")
    public void addNonPayingParentGuardianToCreatedDefendantAccount() {
        Response accountResponse = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        assertEquals(200, accountResponse.statusCode(), "Expected account status request to succeed");
        String etag = accountResponse.getHeader(HttpHeaders.ETAG);
        assertFalse(etag == null || etag.isBlank(), "Expected account status response ETag");

        authorisedJsonRequest()
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header(HttpHeaders.IF_MATCH, etag)
            .body(nonPayingParentGuardianRequest())
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
