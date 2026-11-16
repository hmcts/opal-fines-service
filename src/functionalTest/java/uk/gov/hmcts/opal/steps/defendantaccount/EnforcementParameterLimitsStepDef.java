package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises configured text boundaries through the public add-enforcement and retrieval APIs (PO-11198).
 */
public class EnforcementParameterLimitsStepDef extends BaseStepDef {
    private final DefendantAccountEnforcementsActions actions = new DefendantAccountEnforcementsActions();
    private final Map<String, String> submittedResponses = new LinkedHashMap<>();
    private final Map<String, Integer> expectedTextLengths = new LinkedHashMap<>();
    private JSONObject request;
    private boolean paymentTermsAllowed;

    @Given("a maximum-length enforcement fixture for {string} in {string}")
    public void prepareFixture(String resultId, String language) throws IOException, JSONException {
        buildFixture(resultId, language, true);
    }

    @Given("an ordinary-length enforcement fixture for {string} in {string}")
    public void prepareOrdinaryFixture(String resultId, String language) throws IOException, JSONException {
        buildFixture(resultId, language, false);
    }

    private void buildFixture(String resultId, String language, boolean maximumLength)
        throws IOException, JSONException {
        assertTrue(List.of("English", "Welsh").contains(language), "Unknown fixture language");
        Response mode = authorisedJsonRequest().get(getTestUrl() + "/testing-support/is-legacy-mode");
        mode.then().statusCode(200);
        final boolean legacyMode = Boolean.parseBoolean(mode.asString());
        Response configuration = authorisedJsonRequest()
            .queryParam("include_welsh", language.equals("Welsh"))
            .get(getTestUrl() + "/results/" + resultId);
        configuration.then().statusCode(200);
        paymentTermsAllowed = configuration.jsonPath().getBoolean("allow_payment_terms");
        JSONArray parameters = new JSONArray(configuration.jsonPath().getString("result_parameters"));
        JSONObject values = loadFixtures().getJSONObject(resultId);
        submittedResponses.clear();
        expectedTextLengths.clear();
        for (int index = 0; index < parameters.length(); index++) {
            JSONObject parameter = parameters.getJSONObject(index);
            String name = parameter.getString("name");
            String baseName = name.startsWith("cy_") ? name.substring(3) : name;
            if (parameter.getString("type").startsWith("text")) {
                int limit = parameter.getInt("max");
                // Different start/end markers catch truncation, padding and replacement of equal-length values.
                String sample = name.startsWith("cy_") ? "Taliad ŵ â ê î ô û ŷ " : name + " payment ";
                int length = maximumLength ? limit : Math.min(limit, sample.strip().length());
                String text = sample.repeat(length / sample.length() + 1).substring(0, length - 1) + "!";
                if (name.equals("timeofrelease")) {
                    text = maximumLength ? "14.30 GMT." : "14.30";
                }
                assertTrue(text.length() >= parameter.optInt("min", 0) && text.length() <= limit,
                    name + " fixture must respect the configured length range");
                if (maximumLength) {
                    assertEquals(limit, text.length(), name + " fixture length");
                }
                expectedTextLengths.put(name, text.length());
                submittedResponses.put(name, text);
            } else if (values.has(baseName)) {
                JSONObject modeValues = values.optJSONObject(baseName);
                String value = modeValues == null ? values.getString(baseName)
                    : modeValues.getString(legacyMode ? "legacy" : "opal");
                submittedResponses.put(name, value);
            } else {
                assertFalse(parameter.optBoolean("mandatory"), "Missing valid fixture for " + name);
            }
        }
        assertFalse(expectedTextLengths.isEmpty(), "Expected editable text parameters for " + resultId);
        if (language.equals("Welsh")) {
            assertTrue(expectedTextLengths.keySet().stream().anyMatch(name -> name.startsWith("cy_")),
                "Expected an applicable Welsh text parameter");
        }
        JSONArray responses = new JSONArray();
        for (Map.Entry<String, String> entry : submittedResponses.entrySet()) {
            responses.put(new JSONObject().put("parameter_name", entry.getKey()).put("response", entry.getValue()));
        }
        request = new JSONObject().put("result_id", resultId).put("enforcement_result_responses", responses);
    }

    @Given("the enforcement fixture includes {string} payment terms")
    public void includePaymentTerms(String type) throws JSONException {
        assertTrue(paymentTermsAllowed, "The selected result must allow optional payment terms");
        assertTrue(List.of("By date", "Instalments").contains(type), "Unknown payment terms fixture");
        boolean instalments = type.equals("Instalments");
        request.put("payment_terms", new JSONObject()
            .put("days_in_default", 14)
            .put("date_days_in_default_imposed", "2030-06-15")
            .put("extension", true)
            .put("reason_for_extension", "Agreed payment extension")
            .put("payment_terms_type", new JSONObject().put("payment_terms_type_code", instalments ? "I" : "B"))
            .put("effective_date", "2030-07-15")
            .put("instalment_period", instalments
                ? new JSONObject().put("instalment_period_code", "W") : JSONObject.NULL)
            .put("lump_sum_amount", instalments ? JSONObject.NULL : 100.50)
            .put("instalment_amount", instalments ? 25.50 : JSONObject.NULL)
            .put("posted_details", JSONObject.NULL));
    }

    @When("I submit the enforcement parameter fixture for business unit {string}")
    public void submitFixture(String businessUnitId) {
        actions.getCreatedDefendantAccountEnforcementStatus().then().log().ifValidationFails().statusCode(200);
        Response response = actions.addEnforcementToCreatedDefendantAccount(businessUnitId, request);
        response.then().log().ifValidationFails().statusCode(200);
        assertEquals(scenarioContext().getCreatedDefendantAccountIdOrFail(),
            response.jsonPath().getString("defendant_account_id"));
        assertNotNull(response.jsonPath().getString("enforcement_id"), "Expected a new enforcement action");
    }

    @Then("the exposed enforcement parameters match the submitted content and lengths")
    public void verifyParameterRoundTrip() throws JSONException {
        Response response = actions.getCreatedDefendantAccountEnforcementStatus();
        response.then().statusCode(200);
        assertEquals(request.getString("result_id"),
            response.jsonPath().getString("last_enforcement_action.enforcement_action.result_id"));
        List<Map<String, String>> actualResponses = response.jsonPath()
            .getList("last_enforcement_action.result_responses");
        Map<String, String> actual = new LinkedHashMap<>();
        if (actualResponses != null) {
            actualResponses.forEach(value -> actual.put(value.get("parameter_name"), value.get("response")));
        }
        // Legacy exposes the reason and enforcer as action fields even when result_responses is absent.
        actual.putIfAbsent("reason", response.jsonPath().getString("last_enforcement_action.reason"));
        String enforcerId = response.jsonPath().getString("last_enforcement_action.enforcer.enforcer_id");
        if (enforcerId != null) {
            actual.putIfAbsent("enforcer", enforcerId);
        }
        for (Map.Entry<String, String> entry : submittedResponses.entrySet()) {
            if (!actual.containsKey(entry.getKey())) {
                continue;
            }
            String value = actual.get(entry.getKey());
            assertNotNull(value, "Exposed parameter " + entry.getKey() + " must retain its submitted value");
            if (expectedTextLengths.containsKey(entry.getKey())) {
                assertEquals(expectedTextLengths.get(entry.getKey()).intValue(), value.length(),
                    entry.getKey() + " length");
            }
            assertEquals(entry.getValue(), value, entry.getKey() + " content");
        }
        if (submittedResponses.containsKey("earliestreleasedate")) {
            verifyReleaseDateInHistory();
        }
    }

    private void verifyReleaseDateInHistory() throws JSONException {
        Response response = authorisedJsonRequest().queryParam("itemTypes", "enforcement")
            .get(getTestUrl() + "/defendant-accounts/"
                + scenarioContext().getCreatedDefendantAccountIdOrFail() + "/history");
        response.then().log().ifValidationFails().statusCode(200);
        List<Map<String, Object>> items = response.jsonPath().getList("historyItems.details");
        assertNotNull(items, "Expected enforcement history");
        List<Map<String, Object>> matchingDetails = items.stream()
            .filter(details -> details != null
                && request.optString("result_id").equals(details.get("enforcementAction")))
            .toList();
        assertEquals(1, matchingDetails.size(), "Expected the newly created enforcement in history");
        assertEquals(submittedResponses.get("earliestreleasedate").substring(0, 10),
            matchingDetails.getFirst().get("earliestDateOfRelease"), "Release date in enforcement history");
    }

    @Then("the payment terms GET preserves the submitted enforcement payment terms")
    public void verifyPaymentTermsRoundTrip() throws JSONException {
        Response response = authorisedJsonRequest().get(getTestUrl() + "/defendant-accounts/"
            + scenarioContext().getCreatedDefendantAccountIdOrFail() + "/payment-terms/latest");
        response.then().log().ifValidationFails().statusCode(200);
        JSONObject expected = request.getJSONObject("payment_terms");
        for (String field : List.of("days_in_default", "date_days_in_default_imposed", "extension",
            "reason_for_extension", "effective_date", "payment_terms_type.payment_terms_type_code",
            "instalment_period.instalment_period_code", "lump_sum_amount", "instalment_amount")) {
            Object expectedValue = expected;
            for (String part : field.split("\\.")) {
                expectedValue = expectedValue instanceof JSONObject object ? object.opt(part) : null;
            }
            Object actual = response.jsonPath().get("payment_terms." + field);
            if (expectedValue instanceof Number number) {
                assertNotNull(actual, field);
                assertEquals(number.doubleValue(), ((Number) actual).doubleValue(), field);
            } else {
                assertEquals(expectedValue == JSONObject.NULL ? null : expectedValue, actual, field);
            }
        }
    }

    private JSONObject loadFixtures() throws IOException, JSONException {
        String path = "/features/opalMode/defendantAccounts/enforcementParameterFixtures.json";
        try (InputStream resource = getClass().getResourceAsStream(path)) {
            assertNotNull(resource, "Missing enforcement parameter fixtures");
            return new JSONObject(new String(resource.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
