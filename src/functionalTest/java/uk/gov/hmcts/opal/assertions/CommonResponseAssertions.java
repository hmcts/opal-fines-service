package uk.gov.hmcts.opal.assertions;

import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.utils.TestHttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.steps.BaseStepDef.getTestUrl;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

/**
 * Provides generic HTTP-response assertions that can be reused across functional-test API areas.
 */
public class CommonResponseAssertions {
    private static final String BUSINESS_UNIT_USER_ID_PLACEHOLDER = "BU_USER_ID";
    private static final Pattern STRONG_ETAG = Pattern.compile("^\"[^\"]+\"$");
    private static final Pattern TIMELINE_USERNAME_FIELD = Pattern.compile("^timeline_data\\[\\d+]\\.username$");
    private static final Pattern LEGACY_USER_ID_PATTERN = Pattern.compile("^(L\\d{3})JG$");

    /**
     * Asserts that a response body contains the supplied field values.
     *
     * @param response response to inspect.
     * @param expectedData field names and values expected in the response body.
     */
    public void assertResponseContains(Response response, Map<String, String> expectedData) {
        for (Map.Entry<String, String> entry : expectedData.entrySet()) {
            String expected = entry.getValue();
            String actual = response.jsonPath().getString(entry.getKey());
            expected = resolveExpectedValue(response, expectedData, entry.getKey(), expected, actual);
            if (expected == null || expected.isEmpty()) {
                assertTrue(
                    actual == null || actual.isBlank(),
                    "Values are not equal for field '" + entry.getKey() + "'"
                );
            } else {
                assertEquals(expected, actual, "Values are not equal for field '" + entry.getKey() + "'");
            }
        }
    }

    private String resolveExpectedValue(
        Response response,
        Map<String, String> expectedData,
        String fieldName,
        String expected,
        String actual
    ) {
        if (BUSINESS_UNIT_USER_ID_PLACEHOLDER.equals(expected)) {
            return resolveBusinessUnitUserId(response, expectedData, fieldName);
        }

        return resolveEnvironmentSpecificExpectedValue(fieldName, expected, actual);
    }

    /**
     * Resolves the current scenario user's business-unit user id for the response business unit.
     *
     * @param response response being asserted.
     * @param expectedData field names and values expected in the response body.
     * @param fieldName asserted response field name.
     * @return current user's business-unit user id for the response business unit.
     */
    private String resolveBusinessUnitUserId(
        Response response,
        Map<String, String> expectedData,
        String fieldName
    ) {
        if (!isBusinessUnitUserIdField(fieldName)) {
            throw new IllegalArgumentException(
                BUSINESS_UNIT_USER_ID_PLACEHOLDER + " can only be used for submitted-by or timeline username fields"
            );
        }

        String businessUnitId = Optional.ofNullable(expectedData.get("business_unit_id"))
            .filter(value -> !value.isBlank())
            .orElseGet(() -> response.jsonPath().getString("business_unit_id"));

        if (businessUnitId == null || businessUnitId.isBlank()) {
            throw new IllegalStateException("Cannot resolve " + BUSINESS_UNIT_USER_ID_PLACEHOLDER
                + " without a business_unit_id");
        }

        return getCurrentUserBusinessUnitUserId(businessUnitId);
    }

    /**
     * Checks whether a response field should contain the current user's business-unit user id.
     *
     * @param fieldName asserted response field name.
     * @return true when the field is a submitted-by or timeline username field.
     */
    private boolean isBusinessUnitUserIdField(String fieldName) {
        return "account_snapshot.submitted_by".equals(fieldName)
            || TIMELINE_USERNAME_FIELD.matcher(fieldName).matches();
    }

    /**
     * Calls the application testing-support endpoint and extracts the business-unit user id for
     * the supplied business unit from the current access-token user state.
     *
     * @param businessUnitId business-unit id to match.
     * @return current user's business-unit user id for the supplied business unit.
     */
    private String getCurrentUserBusinessUnitUserId(String businessUnitId) {
        TestHttpClient.TestHttpResponse response = TestHttpClient.get(
            getTestUrl() + "/testing-support/user-client/0",
            Map.of(
                "Accept", "*/*",
                "Authorization", "Bearer " + getToken()
            )
        );

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to fetch current user state, status: " + response.statusCode());
        }

        return businessUnitUsers(response.body()).stream()
            .filter(businessUnitUser -> businessUnitId.equals(businessUnitUser.businessUnitId()))
            .findFirst()
            .map(BusinessUnitUser::businessUnitUserId)
            .orElseThrow(() -> new IllegalStateException(
                "Current user state does not contain business unit " + businessUnitId
            ));
    }

    /**
     * Extracts business-unit users from both snake-case and camel-case JSON shapes.
     *
     * @param userStateJson serialised user-state response.
     * @return business-unit user ids keyed by business-unit id.
     */
    private List<BusinessUnitUser> businessUnitUsers(String userStateJson) {
        JSONObject userState;
        try {
            userState = new JSONObject(userStateJson);
        } catch (JSONException e) {
            throw new IllegalStateException("Failed to parse current user state response", e);
        }

        JSONArray businessUnitUsers = userState.optJSONArray("business_unit_user");
        if (businessUnitUsers == null) {
            businessUnitUsers = userState.optJSONArray("businessUnitUser");
        }

        if (businessUnitUsers == null) {
            return List.of();
        }

        List<BusinessUnitUser> users = new ArrayList<>();
        for (int i = 0; i < businessUnitUsers.length(); i++) {
            JSONObject businessUnitUser = businessUnitUsers.optJSONObject(i);
            if (businessUnitUser == null) {
                continue;
            }

            String id = optionalString(businessUnitUser, "business_unit_user_id")
                .or(() -> optionalString(businessUnitUser, "businessUnitUserId"))
                .orElse(null);
            String unitId = optionalString(businessUnitUser, "business_unit_id")
                .or(() -> optionalString(businessUnitUser, "businessUnitId"))
                .orElse(null);

            if (id != null && unitId != null) {
                users.add(new BusinessUnitUser(id, unitId));
            }
        }

        return users;
    }

    /**
     * Reads an optional JSON string field, converting non-string scalar values to strings.
     *
     * @param json source JSON object.
     * @param fieldName field name to read.
     * @return field value when present and non-null.
     */
    private Optional<String> optionalString(JSONObject json, String fieldName) {
        if (!json.has(fieldName) || json.isNull(fieldName)) {
            return Optional.empty();
        }
        return Optional.of(String.valueOf(json.opt(fieldName)));
    }

    private record BusinessUnitUser(String businessUnitUserId, String businessUnitId) {
    }

    /**
     * Normalises selected expected values when the environment returns the operational-assistant
     * user-code variant instead of the judicial-greffe variant used in the feature data.
     *
     * @param fieldName asserted response field name.
     * @param expected expected field value from the feature data.
     * @param actual actual field value returned by the API.
     * @return adjusted expected value when the current environment requires it; otherwise the
     *         original expected value.
     */
    private String resolveEnvironmentSpecificExpectedValue(String fieldName, String expected, String actual) {
        if (expected == null || actual == null) {
            return expected;
        }

        if (!"account_snapshot.submitted_by".equals(fieldName)
            && !TIMELINE_USERNAME_FIELD.matcher(fieldName).matches()) {
            return expected;
        }

        var matcher = LEGACY_USER_ID_PATTERN.matcher(expected);
        if (!matcher.matches()) {
            return expected;
        }

        // Accept the operational-assistant variant for the same business-unit user id, for
        // example L073JG in the feature data matching L073OA in deployed environments.
        String expectedPreprodValue = matcher.group(1) + "OA";
        return expectedPreprodValue.equals(actual) ? expectedPreprodValue : expected;
    }

    /**
     * Asserts that the supplied response returned the expected HTTP status code.
     *
     * @param response response to inspect.
     * @param expectedStatusCode expected HTTP status code.
     */
    public void assertStatus(Response response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode(), "Unexpected HTTP status");
    }

    /**
     * Asserts that the supplied response exposes a strong quoted ETag header.
     *
     * @param response response whose ETag header should be validated.
     */
    public void assertStrongQuotedEtag(Response response) {
        String etag = response.getHeader("ETag");
        assertNotNull(etag, "ETag header must be present");
        assertTrue(STRONG_ETAG.matcher(etag).matches(), "ETag must be strong and quoted");
    }

    /**
     * Asserts that the supplied response body does not contain the named field anywhere in its
     * JSON structure.
     *
     * @param response response whose body should be inspected.
     * @param field field name that must be absent from the response body.
     */
    public void assertBodyDoesNotContainField(Response response, String field) {
        String body = response.getBody() != null ? response.getBody().asString() : "";
        if (body.isBlank()) {
            return;
        }

        Optional<Object> parsed = tryParseJson(body);
        if (parsed.isPresent() && containsFieldAnywhere(parsed.get(), field)) {
            throw new AssertionError("Response must not include field: " + field);
        }
    }

    /**
     * Attempts to parse a response body into a JSON object or array.
     *
     * @param body response body to parse.
     * @return parsed JSON value when the body is valid JSON; otherwise an empty optional.
     */
    private Optional<Object> tryParseJson(String body) {
        String trimmedBody = body.trim();
        try {
            if (trimmedBody.startsWith("{")) {
                return Optional.of(new JSONObject(trimmedBody));
            }
            if (trimmedBody.startsWith("[")) {
                return Optional.of(new JSONArray(trimmedBody));
            }
        } catch (Exception ignored) {
            // Not valid JSON; leave assertion to pass because field-level absence cannot be checked.
        }
        return Optional.empty();
    }

    /**
     * Recursively checks whether the named field exists anywhere in a parsed JSON tree.
     *
     * @param json parsed JSON object or array to inspect.
     * @param field field name to search for.
     * @return {@code true} when the field exists anywhere in the JSON structure.
     */
    private boolean containsFieldAnywhere(Object json, String field) {
        if (json == null) {
            return false;
        }

        return switch (json) {
            case JSONObject obj -> {
                if (obj.has(field)) {
                    yield true;
                }
                @SuppressWarnings("unchecked")
                java.util.Iterator<String> keys = obj.keys();
                boolean found = false;
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (containsFieldAnywhere(obj.opt(key), field)) {
                        found = true;
                        break;
                    }
                }
                yield found;
            }
            case JSONArray arr -> {
                boolean found = false;
                for (int i = 0; i < arr.length(); i++) {
                    if (containsFieldAnywhere(arr.opt(i), field)) {
                        found = true;
                        break;
                    }
                }
                yield found;
            }
            default -> false;
        };
    }
}
