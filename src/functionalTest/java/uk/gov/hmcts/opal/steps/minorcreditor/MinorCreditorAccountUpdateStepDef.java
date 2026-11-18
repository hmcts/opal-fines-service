package uk.gov.hmcts.opal.steps.minorcreditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.actions.minorcreditor.MinorCreditorHistoryFixtureActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Exercises minor-creditor amendments and conversions using scenario-owned accounts.
 */
public class MinorCreditorAccountUpdateStepDef extends BaseStepDef {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String UPDATED_NAME = "PDPO Updated";
    private final MinorCreditorHistoryFixtureActions fixtureActions = new MinorCreditorHistoryFixtureActions();

    private Long accountId;
    private String businessUnitId;
    private boolean company;
    private BigInteger initialVersion;
    private Response updateResponse;
    private ObjectNode maximumLengthRequest;
    private final Map<String, Integer> expectedLengths = new LinkedHashMap<>();

    /**
     * Creates a unique minor creditor and prepares its account type for the requested scenario.
     *
     * @param accountType individual or company.
     * @throws JSONException if the fixture request cannot be created.
     */
    @Given("a(n) {word} minor creditor account exists for amendment")
    public void createMinorCreditor(String accountType) throws JSONException {
        assertTrue("individual".equals(accountType) || "company".equals(accountType), "Unsupported account type");
        company = "company".equals(accountType);
        Response fixture = fixtureActions.createFixture("PDPOMC" + System.nanoTime());
        assertEquals(200, fixture.statusCode(), "Expected minor-creditor fixture creation to succeed");
        JsonNode body = MAPPER.readTree(fixture.asString());
        accountId = body.path("creditor_account_id").asLong();
        assertTrue(accountId > 0, "Expected a created creditor account ID");
        businessUnitId = body.path("business_unit_id").asString();
        scenarioContext().setCreatedMinorCreditorAccountId(accountId);

        if (company) {
            Response current = getAccount();
            ObjectNode request = updateRequest(current);
            ObjectNode party = (ObjectNode) request.path("party_details");
            party.put("organisation_flag", true);
            party.remove("individual_details");
            party.putObject("organisation_details").put("organisation_name", "PDPO Company");
            assertEquals(200, patchAccount(request, current.getHeader(HttpHeaders.ETAG)).statusCode(),
                "Expected company fixture preparation to succeed");
        }
    }

    /**
     * Updates party, address and payment data using the account's current ETag.
     */
    @When("I update the created minor creditor account details")
    public void updateMinorCreditor() {
        Response current = getAccount();
        String etag = current.getHeader(HttpHeaders.ETAG);
        initialVersion = new BigInteger(etag.replace("\"", ""));
        ObjectNode request = updateRequest(current);
        ObjectNode party = (ObjectNode) request.path("party_details");
        ObjectNode details = (ObjectNode) party.path(company ? "organisation_details" : "individual_details");
        details.put(company ? "organisation_name" : "surname", UPDATED_NAME);
        ((ObjectNode) request.path("address")).put("address_line_1", "99 PDPO Road").put("postcode", "NW1 1AA");
        ((ObjectNode) request.path("payment")).put("account_name", UPDATED_NAME)
            .put("account_reference", "PDPO-UPDATE").put("hold_payment", true);

        updateResponse = patchAccount(request, etag);
    }

    /**
     * Verifies the PATCH response and a subsequent GET expose the updated data and existing version increments.
     */
    @Then("the minor creditor update returns the changed details and preserves version behaviour")
    public void assertUpdatedMinorCreditor() {
        assertNotNull(updateResponse, "No minor-creditor update response is available");
        assertEquals(200, updateResponse.statusCode(),
            "Expected minor-creditor update success: " + updateResponse.asString());
        assertTrue(updateResponse.contentType().startsWith("application/json"));
        assertEquals("\"" + initialVersion.add(BigInteger.ONE) + "\"", updateResponse.getHeader(HttpHeaders.ETAG));
        assertUpdatedDetails(MAPPER.readTree(updateResponse.asString()));

        Response persisted = getAccount();
        assertEquals("\"" + initialVersion.add(BigInteger.TWO) + "\"", persisted.getHeader(HttpHeaders.ETAG));
        assertUpdatedDetails(MAPPER.readTree(persisted.asString()));
    }

    /**
     * Submits an amendment or conversion with the applicable values from the boundary fixture.
     *
     * @param targetType individual or company after the update.
     * @param accountNumber BACS account number, or "none" for non-BACS payment.
     * @param fixture explicit field values and their expected lengths.
     */
    @When("I amend the minor creditor to a(n) {word} with BACS account number {string} and maximum-length details")
    public void updateMaximumLengthDetails(String targetType, String accountNumber, DataTable fixture) {
        assertTrue("individual".equals(targetType) || "company".equals(targetType), "Unsupported target type");
        Response current = getAccount();
        assertEquals(company, MAPPER.readTree(current.asString()).at("/party_details/organisation_flag").asBoolean(),
            "Expected the source account type before amendment or conversion");
        company = "company".equals(targetType);
        maximumLengthRequest = updateRequest(current);
        ObjectNode party = (ObjectNode) maximumLengthRequest.path("party_details");
        party.put("organisation_flag", company);
        party.remove("individual_details");
        party.remove("organisation_details");
        party.putObject(company ? "organisation_details" : "individual_details");
        ObjectNode payment = (ObjectNode) maximumLengthRequest.path("payment");
        payment.remove("account_name");
        payment.remove("account_reference");
        payment.remove("sort_code");
        payment.remove("account_number");
        boolean bacs = !"none".equals(accountNumber);
        payment.put("pay_by_bacs", bacs);
        if (bacs) {
            assertTrue(accountNumber.matches("[0-9]{6,8}"), "Expected a 6-8 digit BACS account number");
            payment.put("account_number", accountNumber);
            expectedLengths.put("/payment/account_number", accountNumber.length());
        }
        applyMaximumLengthFixture(fixture, bacs);
        updateResponse = patchAccount(maximumLengthRequest, current.getHeader(HttpHeaders.ETAG));
    }

    /**
     * Checks success and reads the account back to prove content and length survive persistence.
     */
    @Then("the minor creditor amendment succeeds and GET returns the submitted maximum-length details")
    public void assertMaximumLengthDetails() {
        assertNotNull(updateResponse, "No minor-creditor update response is available");
        assertEquals(200, updateResponse.statusCode(), "Expected PATCH success: " + updateResponse.asString());
        JsonNode persisted = MAPPER.readTree(getAccount().asString());
        assertEquals(accountId.longValue(), persisted.path("creditor_account_id").asLong());
        assertEquals(company, persisted.at("/party_details/organisation_flag").asBoolean());
        JsonNode inactiveParty = persisted.at(company ? "/party_details/individual_details"
            : "/party_details/organisation_details");
        assertTrue(inactiveParty.isNull() || inactiveParty.isMissingNode(),
            "Expected only the applicable party details");
        expectedLengths.forEach((path, length) -> {
            assertEquals(maximumLengthRequest.at(path), persisted.at(path), "Changed content at " + path);
            assertEquals(length.intValue(), persisted.at(path).asString().length(), "Changed length at " + path);
        });
        assertEquals(maximumLengthRequest.at("/payment/pay_by_bacs"), persisted.at("/payment/pay_by_bacs"));
        assertEquals(maximumLengthRequest.at("/payment/hold_payment"), persisted.at("/payment/hold_payment"));
    }

    /**
     * Removes all fixture data, including amendments produced by the update.
     */
    @After("@MinorCreditorUpdatePdpo or @MinorCreditorMaximumLength")
    public void cleanUpMinorCreditor() {
        if (accountId != null) {
            Response response = fixtureActions.deleteFixture(accountId);
            assertTrue(response.statusCode() == 204 || response.statusCode() == 404,
                "Unexpected minor-creditor fixture cleanup status: " + response.statusCode());
        }
    }

    private void applyMaximumLengthFixture(DataTable fixture, boolean bacs) {
        for (Map<String, String> row : fixture.asMaps()) {
            String group = row.get("group");
            if (("individual".equals(group) && company) || ("company".equals(group) && !company)
                || ("BACS".equals(group) && !bacs)) {
                continue;
            }
            String path = switch (group) {
                case "individual" -> "/party_details/individual_details";
                case "company" -> "/party_details/organisation_details";
                case "address" -> "/address";
                case "BACS" -> "/payment";
                default -> throw new IllegalArgumentException("Unsupported fixture group: " + group);
            };
            String field = row.get("field");
            String value = row.get("value");
            int length = Integer.parseInt(row.get("length"));
            assertEquals(length, value.length(), "Incorrect fixture length for " + field);
            ((ObjectNode) maximumLengthRequest.at(path)).put(field, value);
            expectedLengths.put(path + "/" + field, length);
        }
    }

    private Response getAccount() {
        Response response = authorisedJsonRequest().when().get(getTestUrl() + "/minor-creditor-accounts/" + accountId);
        assertEquals(200, response.statusCode(), "Expected minor-creditor GET success: " + response.asString());
        assertNotNull(response.getHeader(HttpHeaders.ETAG), "Expected an account ETag");
        return response;
    }

    private ObjectNode updateRequest(Response response) {
        JsonNode account = MAPPER.readTree(response.asString());
        ObjectNode request = MAPPER.createObjectNode();
        for (String field : new String[] {"party_details", "address", "payment"}) {
            assertTrue(account.path(field).isObject(), "Expected minor-creditor response group: " + field);
            request.set(field, account.path(field).deepCopy());
        }
        return request;
    }

    private Response patchAccount(ObjectNode request, String etag) {
        return authorisedJsonRequest()
            .header("Business-Unit-Id", businessUnitId)
            .header(HttpHeaders.IF_MATCH, etag)
            .body(request.toString())
            .when().patch(getTestUrl() + "/minor-creditor-accounts/" + accountId);
    }

    private void assertUpdatedDetails(JsonNode response) {
        assertEquals(accountId.longValue(), response.path("creditor_account_id").asLong());
        JsonNode party = response.path("party_details");
        assertEquals(company, party.path("organisation_flag").asBoolean());
        JsonNode details = party.path(company ? "organisation_details" : "individual_details");
        assertEquals(UPDATED_NAME, details.path(company ? "organisation_name" : "surname").asString());
        assertEquals("99 PDPO Road", response.path("address").path("address_line_1").asString());
        assertEquals("NW1 1AA", response.path("address").path("postcode").asString());
        assertEquals(UPDATED_NAME, response.path("payment").path("account_name").asString());
        assertEquals("PDPO-UPDATE", response.path("payment").path("account_reference").asString());
        assertTrue(response.path("payment").path("hold_payment").asBoolean());
    }
}
