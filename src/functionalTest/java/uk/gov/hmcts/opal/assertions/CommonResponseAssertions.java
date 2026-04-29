package uk.gov.hmcts.opal.assertions;

import io.restassured.response.Response;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Provides generic HTTP-response assertions that can be reused across functional-test API areas.
 */
public class CommonResponseAssertions {
    private static final Pattern STRONG_ETAG = Pattern.compile("^\"[^\"]+\"$");

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
