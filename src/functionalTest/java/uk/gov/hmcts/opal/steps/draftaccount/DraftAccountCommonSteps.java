package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.response.Response;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.config.Constants;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Common test-level helpers and reusable assertions for Draft-Account API tests.
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>{@link #fetchStrongEtag(String, String)} → GET + return strong quoted ETag (or {@code null} on 404)</li>
 *   <li>{@link #isStrongEtag(String)} → validate strong, quoted ETag format</li>
 *   <li>Recursive JSON check to ensure a field is absent anywhere in the body</li>
 *   <li>Step defs:
 *     <ul>
 *       <li>{@code Then the response must include a strong quoted ETag header}</li>
 *       <li>{@code Then the response body must not include the "<field>" field anywhere}</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class DraftAccountCommonSteps {

    private static final Logger log = LoggerFactory.getLogger(DraftAccountCommonSteps.class);

    /**
     * Strong, quoted ETag pattern.
     * Example: {@code "\"42\""}.
     */
    private static final Pattern STRONG_ETAG = Pattern.compile("^\"[^\"]+\"$");

    // ──────────────────────────────  Shared helpers  ──────────────────────────────

    /**
     * Determine whether an ETag value is strong and quoted.
     *
     * @param etag the ETag header value (may be {@code null})
     * @return {@code true} if the value matches the strong, quoted pattern
     *         (e.g., {@code "\"42\""}), otherwise {@code false}
     */
    public static boolean isStrongEtag(String etag) {
        return etag != null && STRONG_ETAG.matcher(etag).matches();
    }

    /**
     * Fetch the ETag for a draft account by issuing a GET to {@code /draft-accounts/{id}}.
     *
     * <p>Returns a strong, quoted ETag string when the resource exists. Returns {@code null} if the resource
     * is not found (HTTP 404).</p>
     *
     * @param baseUrl base URL for the API under test
     * @param id the draft account identifier
     * @return strong, quoted ETag value; or {@code null} if the resource does not exist
     */
    public static String fetchStrongEtag(String baseUrl, String id) {
        Response r = SerenityRest.given()
            .header("Authorization", "Bearer " + BearerTokenStepDef.getToken())
            .accept("application/json")
            .get(baseUrl + Constants.DRAFT_ACCOUNTS_URI + "/" + id);

        int code = r.getStatusCode();
        if (code == 404) {
            log.info("GET {}{} → 404 (no ETag)", baseUrl, Constants.DRAFT_ACCOUNTS_URI + "/" + id);
            return null;
        }

        assertThat("GET for ETag should be 200 or 304", code, anyOf(is(200), is(304)));

        String etag = r.getHeader("ETag");
        assertThat("ETag must be present", etag, notNullValue());
        assertThat("ETag must be strong and quoted (e.g., \"42\")", isStrongEtag(etag), is(true));
        return etag;
    }

    // ──────────────────────────────  JSON utilities  ──────────────────────────────

    /**
     * Attempt to parse a response body as JSON into {@link JSONObject} or {@link JSONArray}.
     *
     * @param body raw response body (may be {@code null})
     * @return parsed JSON container or {@link Optional#empty()} if not JSON
     */
    private static Optional<Object> tryParseJson(String body) {
        if (body == null) {
            return Optional.empty();
        }
        String t = body.trim();
        try {
            if (t.startsWith("{")) {
                return Optional.of(new JSONObject(t));
            }
            if (t.startsWith("[")) {
                return Optional.of(new JSONArray(t));
            }
        } catch (Exception ignored) {
            // Not valid JSON; skip assertions
        }
        return Optional.empty();
    }

    /**
     * Recursively test whether a field exists anywhere in a {@link JSONObject}/{@link JSONArray} tree.
     *
     * @param json  parsed JSON object/array (or primitive/null)
     * @param field field name to search for
     * @return {@code true} if the field appears at any depth; otherwise {@code false}
     */
    private static boolean containsFieldAnywhere(Object json, String field) {
        if (json == null) {
            return false;
        }

        return switch (json) {
            case JSONObject obj -> {
                if (obj.has(field)) {
                    yield true;
                }
                @SuppressWarnings("unchecked")
                Iterator<String> keys = obj.keys();
                boolean found = false;
                while (keys.hasNext()) {
                    String k = keys.next();
                    Object v = obj.opt(k); // safe accessor
                    if (containsFieldAnywhere(v, field)) {
                        found = true;
                        break;
                    }
                }
                yield found;
            }
            case JSONArray arr -> {
                boolean found = false;
                for (int i = 0; i < arr.length(); i++) {
                    Object v = arr.opt(i); // safe accessor
                    if (containsFieldAnywhere(v, field)) {
                        found = true;
                        break;
                    }
                }
                yield found;
            }
            default -> false; // primitives/strings/other
        };
    }

    // ──────────────────────────────  Step definitions  ──────────────────────────────

    /**
     * Verify the last response includes a strong, quoted ETag header.
     */
    @Then("the response must include a strong quoted ETag header")
    public void responseHasStrongQuotedEtag() {
        Response r = SerenityRest.lastResponse();
        String etag = r.getHeader("ETag");
        assertThat("ETag header must be present", etag, notNullValue());
        assertThat("ETag must be strong and quoted", isStrongEtag(etag), is(true));
    }

    /**
     * Verify the last response body does not contain the specified field anywhere in the JSON structure.
     *
     * @param field the property name that must not appear in the response body
     */
    @Then("the response body must not include the {string} field anywhere")
    public void responseBodyMustNotIncludeFieldAnywhere(String field) {
        Response r = SerenityRest.lastResponse();
        String body = (r.getBody() != null) ? r.getBody().asString() : "";
        if (body.isBlank()) {
            return;
        }

        Optional<Object> parsed = tryParseJson(body);
        if (parsed.isPresent() && containsFieldAnywhere(parsed.get(), field)) {
            Assertions.fail("Response must not include field: " + field);
        }
    }

    /**
     * Remember the ETag from the last response under a named key for later reuse.
     */
    @And("I remember the last response ETag as {string}")
    public void rememberLastResponseEtagAs(String name) {
        Response r = SerenityRest.lastResponse();
        String etag = (r != null) ? r.getHeader("ETag") : null;
        if (etag == null || etag.isBlank()) {
            Assertions.fail("No ETag available to remember");
        }
        Serenity.setSessionVariable("etag:" + name).to(etag);
    }
}
