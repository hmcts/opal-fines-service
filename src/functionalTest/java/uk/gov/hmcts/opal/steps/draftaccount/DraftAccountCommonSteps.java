package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.draftaccount.DraftAccountEtagHelper;

import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Common test-level helpers and reusable assertions for Draft-Account API tests.
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>{@link DraftAccountEtagHelper#fetchStrongEtag(String, String)} → GET + return strong quoted ETag (or
 *   {@code null} on 404)</li>
 *   <li>{@link DraftAccountEtagHelper#isStrongEtag(String)} → validate strong, quoted ETag format</li>
 *   <li>Recursive JSON check to ensure a field is absent anywhere in the body</li>
 *   <li>Step defs:
 *     <ul>
 *       <li>{@code Then the response must include a strong quoted ETag header}</li>
 *       <li>{@code Then the response body must not include the "<field>" field anywhere}</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class DraftAccountCommonSteps extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(DraftAccountCommonSteps.class);

    // ──────────────────────────────  JSON utilities  ──────────────────────────────

    /**
     * Attempts to parse a response body as JSON into {@link JSONObject} or {@link JSONArray}.
     *
     * @param body response body to parse.
     * @return parsed JSON value when the body contains valid JSON; otherwise an empty optional.
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
     * Recursively tests whether a field exists anywhere in a {@link JSONObject}/{@link JSONArray}
     * tree.
     *
     * @param json parsed JSON object or array to inspect.
     * @param field field name to search for in the response body.
     * @return true if the field exists anywhere in the JSON tree; otherwise false.
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
     * Asserts that the response must include a strong quoted ETag header.
     */
    @Then("the response must include a strong quoted ETag header")
    public void responseHasStrongQuotedEtag() {
        Response r = SerenityRest.lastResponse();
        String etag = r.getHeader("ETag");
        log.debug("Validating strong quoted ETag header: {}", etag);
        assertThat("ETag header must be present", etag, notNullValue());
        assertThat("ETag must be strong and quoted", DraftAccountEtagHelper.isStrongEtag(etag), is(true));
    }

    /**
     * Asserts that the response body does not include the supplied field anywhere in the JSON
     * structure.
     *
     * @param field field name to search for in the response body.
     */
    @Then("the response body must not include the {string} field anywhere")
    public void responseBodyMustNotIncludeFieldAnywhere(String field) {
        Response r = SerenityRest.lastResponse();
        String body = (r.getBody() != null) ? r.getBody().asString() : "";
        if (body.isBlank()) {
            log.debug("Skipping field absence check for '{}' because the response body is blank", field);
            return;
        }

        Optional<Object> parsed = tryParseJson(body);
        log.debug("Checking that the latest response body does not contain field '{}'", field);
        if (parsed.isPresent() && containsFieldAnywhere(parsed.get(), field)) {
            Assertions.fail("Response must not include field: " + field);
        }
    }

    /**
     * Stores the ETag from the latest response under a named scenario-context key for later reuse.
     *
     * @param name logical name used to store and retrieve the remembered ETag.
     */
    @And("I remember the last response ETag as {string}")
    public void rememberLastResponseEtagAs(String name) {
        Response r = SerenityRest.lastResponse();
        String etag = (r != null) ? r.getHeader("ETag") : null;
        if (etag == null || etag.isBlank()) {
            Assertions.fail("No ETag available to remember");
        }
        log.info("Remembering last response ETag as '{}'", name);
        scenarioContext().rememberEtag(name, etag);
    }
}
