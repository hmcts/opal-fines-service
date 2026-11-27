package uk.gov.hmcts.opal.steps.defendantaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

/**
 * Proves editable party boundaries survive amendment, conversion and subsequent retrieval.
 */
public class PartyMaximumLengthStepDef extends BaseStepDef {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Map<String, Integer> expectedLengths = new LinkedHashMap<>();
    private String partyId;
    private boolean guardian;
    private boolean reduced;
    private boolean company;
    private ObjectNode request;
    private Response putResponse;

    @Given("a {word} party exists for maximum-length amendment")
    public void createParty(String source) throws Exception {
        assertTrue(List.of("individual", "company", "paying-guardian", "non-paying-guardian").contains(source),
            "Unsupported source party shape: " + source);
        guardian = source.endsWith("guardian");
        reduced = "non-paying-guardian".equals(source);
        String fixture = "paying-guardian".equals(source) ? "parentOrGuardianAccount.json" : "auditableAccount.json";
        new DefendantAccountEnforcementWorkflow().createEnforceableDefendantAccount(Map.of(
            "source", "MAC", "business_unit_id", "77", "account_type", "Fine", "account_status", "Submitted",
            "account", "draftAccounts/accountJson/" + fixture));

        if (reduced) {
            DefendantAccountPartyAddStepDef addSteps = new DefendantAccountPartyAddStepDef();
            addSteps.addNonPayingParentGuardianToCreatedDefendantAccount();
            addSteps.addPartyResponseIdentifiesNonPayingParentGuardianWithPartyId();
        }
        Response header = getHeader();
        partyId = header.jsonPath().getString(guardian ? "parent_guardian_party_id" : "defendant_account_party_id");
        assertNotNull(partyId, "Expected the existing account-party association ID");
        assertFalse(partyId.isBlank(), "Expected a non-blank account-party association ID");

        if (!reduced) {
            prepareSourceParty("company".equals(source));
        }
        JsonNode persisted = getParty();
        assertEquals(guardian ? "Parent/Guardian" : "Defendant",
            persisted.path("defendant_account_party_type").asString());
        assertEquals(!reduced, persisted.path("is_debtor").asBoolean(), "Unexpected source debtor status");
        assertEquals("company".equals(source), persisted.at("/party_details/organisation_flag").asBoolean(),
            "Unexpected source party type before amendment/conversion");
    }

    @When("I replace the party as a {word} using the maximum-length editable fixture")
    public void replaceParty(String target, DataTable fixture) {
        assertTrue(List.of("individual", "company").contains(target), "Unsupported target party shape");
        company = "company".equals(target);
        assertFalse(guardian && company, "Guardian fixtures must remain individual");
        request = partyRequest(company);
        expectedLengths.clear();
        for (Map<String, String> row : fixture.asMaps()) {
            if (applies(row.get("group"))) {
                String path = row.get("path");
                String value = row.get("value");
                int length = Integer.parseInt(row.get("length"));
                assertEquals(length, value.length(), "Invalid fixture length at " + path);
                if (path.contains("telephone_number")) {
                    assertTrue(value.matches("[0-9 ]+"), "Phone fixture must contain only digits and spaces");
                    assertEquals(11, value.replace(" ", "").length(), "Phone fixture must contain 11 digits");
                }
                int separator = path.lastIndexOf('/');
                request.withObject(path.substring(0, separator)).put(path.substring(separator + 1), value);
                expectedLengths.put(path, length);
            }
        }
        if (!reduced && !company) {
            request.withObject("/employer_details/employer_address")
                .putNull("address_line_4").putNull("address_line_5");
        }
        assertFalse(expectedLengths.isEmpty(), "Expected editable boundary values");
        putResponse = putParty(request);
    }

    @Then("the party replacement succeeds and GET preserves the maximum-length editable values")
    public void assertPersistedParty() {
        assertNotNull(putResponse, "Expected a party PUT response");
        assertEquals(200, putResponse.statusCode(), "Party PUT failed: " + putResponse.asString());
        assertNotNull(putResponse.getHeader(HttpHeaders.ETAG), "Expected a replacement ETag");
        JsonNode persisted = getParty();
        assertEquals(request.path("defendant_account_party_type"), persisted.path("defendant_account_party_type"));
        assertEquals(request.path("is_debtor"), persisted.path("is_debtor"));
        assertEquals(company, persisted.at("/party_details/organisation_flag").asBoolean());
        assertEquals(request.at("/party_details/party_id"), persisted.at("/party_details/party_id"));
        expectedLengths.forEach((path, length) -> {
            assertTrue(persisted.at(path).isString(), "Expected persisted text at " + path);
            assertEquals(request.at(path).asString(), persisted.at(path).asString(), "Changed content at " + path);
            assertEquals(length.intValue(), persisted.at(path).asString().length(), "Changed length at " + path);
        });
        if (!reduced) {
            String aliases = company ? "/party_details/organisation_details/organisation_aliases"
                : "/party_details/individual_details/individual_aliases";
            assertEquals(1, persisted.at(aliases).size(), "Expected the submitted alias only");
            assertEquals(1, persisted.at(aliases + "/0/sequence_number").asInt());
            JsonNode aliasId = persisted.at(aliases + "/0/alias_id");
            assertTrue(aliasId.isString() && !aliasId.asString().isBlank(), "Expected a persisted alias ID");
            JsonNode submittedAliasId = request.at(aliases + "/0/alias_id");
            if (!submittedAliasId.isNull()) {
                assertEquals(submittedAliasId, aliasId, "An amended alias must retain its ID");
            }
        }
        JsonNode inactive = persisted.at(company ? "/party_details/individual_details"
            : "/party_details/organisation_details");
        assertTrue(inactive.isMissingNode() || inactive.isNull(), "Expected only the target party details");
    }

    private boolean applies(String group) {
        return switch (group) {
            case "all" -> true;
            case "defendant-address" -> !guardian;
            case "guardian-address" -> guardian;
            case "individual" -> !company;
            case "full-individual" -> !company && !reduced;
            case "company" -> company;
            case "contact", "vehicle" -> !reduced;
            case "employer" -> !reduced && !company;
            default -> throw new IllegalArgumentException("Unsupported fixture group: " + group);
        };
    }

    private ObjectNode partyRequest(boolean organisation) {
        JsonNode current = getParty();
        ObjectNode party = MAPPER.createObjectNode();
        party.set("defendant_account_party_type", current.path("defendant_account_party_type"));
        party.set("is_debtor", current.path("is_debtor"));
        ObjectNode details = party.putObject("party_details");
        details.set("party_id", current.at("/party_details/party_id"));
        details.put("organisation_flag", organisation);
        if (organisation) {
            details.putObject("organisation_details").putArray("organisation_aliases")
                .addObject().putNull("alias_id").put("sequence_number", 1);
        } else {
            ObjectNode individual = details.putObject("individual_details");
            individual.put("title", "Ms");
            individual.putNull("date_of_birth");
            individual.putNull("age");
            individual.putNull("national_insurance_number");
            if (reduced) {
                individual.putNull("individual_aliases");
            } else {
                individual.putArray("individual_aliases").addObject().putNull("alias_id").put("sequence_number", 1);
            }
        }
        if (!reduced && organisation == current.at("/party_details/organisation_flag").asBoolean()) {
            String aliasPath = organisation ? "/party_details/organisation_details/organisation_aliases/0"
                : "/party_details/individual_details/individual_aliases/0";
            JsonNode aliasId = current.at(aliasPath + "/alias_id");
            if (aliasId.isString() && !aliasId.asString().isBlank()) {
                party.withObject(aliasPath).set("alias_id", aliasId);
            }
        }
        ObjectNode address = party.putObject("address");
        address.set("address_line_1", current.at("/address/address_line_1"));
        for (int line = 2; line <= 5; line++) {
            address.putNull("address_line_" + line);
        }
        address.putNull("postcode");
        List<String> optionalSections = List.of("contact_details", "vehicle_details", "employer_details",
            "language_preferences");
        for (String section : optionalSections) {
            party.putNull(section);
        }
        return party;
    }

    private void prepareSourceParty(boolean organisation) {
        ObjectNode preparation = partyRequest(organisation);
        if (organisation) {
            preparation.withObject("/party_details/organisation_details").put("organisation_name", "Original Company");
            preparation.withObject("/party_details/organisation_details/organisation_aliases/0")
                .put("organisation_name", "Original Trading");
        } else {
            preparation.withObject("/party_details/individual_details")
                .put("forenames", "Original").put("surname", "Individual");
            preparation.withObject("/party_details/individual_details/individual_aliases/0")
                .put("forenames", "Original").put("surname", "Alias");
        }
        Response response = putParty(preparation);
        assertEquals(200, response.statusCode(), "Source preparation failed: " + response.asString());
        String aliases = organisation ? "/party_details/organisation_details/organisation_aliases"
            : "/party_details/individual_details/individual_aliases";
        JsonNode persisted = getParty();
        assertEquals(1, persisted.at(aliases).size(), "Expected one source alias before amendment/conversion");
        JsonNode aliasId = persisted.at(aliases + "/0/alias_id");
        assertTrue(aliasId.isString() && !aliasId.asString().isBlank(), "Expected an existing source alias ID");
    }

    private JsonNode getParty() {
        Response response = authorisedJsonRequest().get(partyUrl());
        assertEquals(200, response.statusCode(), "Party GET failed: " + response.asString());
        JsonNode party = MAPPER.readTree(response.asString()).path("defendant_account_party");
        assertTrue(party.isObject(), "Expected a defendant_account_party in GET");
        return party;
    }

    private Response getHeader() {
        Response response = authorisedJsonRequest().get(accountUrl() + "/header-summary");
        assertEquals(200, response.statusCode(), "Header GET failed: " + response.asString());
        assertNotNull(response.getHeader(HttpHeaders.ETAG), "Expected a current account ETag");
        return response;
    }

    private Response putParty(ObjectNode body) {
        String etag = getHeader().getHeader(HttpHeaders.ETAG);
        return authorisedJsonRequest().header("Business-Unit-Id", "77").header(HttpHeaders.IF_MATCH, etag)
            .body(body.toString()).put(partyUrl());
    }

    private String accountUrl() {
        return getTestUrl() + "/defendant-accounts/" + scenarioContext().getCreatedDefendantAccountIdOrFail();
    }

    private String partyUrl() {
        return accountUrl() + "/defendant-account-parties/" + partyId;
    }
}
