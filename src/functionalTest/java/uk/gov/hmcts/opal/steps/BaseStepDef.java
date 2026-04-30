package uk.gov.hmcts.opal.steps;

import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.context.ScenarioContext;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;

import java.util.Map;

import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

/**
 * Provides shared URLs, JSON helpers, and request builders for functional-test step definitions.
 */
public class BaseStepDef {

    private static final String TEST_URL =
        System.getenv().getOrDefault("TEST_URL", "http://localhost:4550");
    private static final String LOGGING_TEST_URL =
        System.getenv().getOrDefault("OPAL_LOGGING_SERVICE_API_URL", "http://localhost:4065");
    private static final String USER_SERVICE_URL = resolveUserServiceUrl();

    /**
     * Returns the base URL for the application under test.
     *
     * @return the base URL for the application under test.
     */
    public static String getTestUrl() {
        return TEST_URL;
    }

    /**
     * Returns the base URL for the logging-service test-support endpoint.
     *
     * @return the base URL for the logging-service test-support endpoint.
     */
    public static String getLoggingTestUrl() {
        return LOGGING_TEST_URL;
    }

    /**
     * Returns the base URL for the user-service test-support endpoint.
     *
     * @return the base URL for the user-service test-support endpoint.
     */
    protected static String getUserServiceUrl() {
        return USER_SERVICE_URL;
    }

    /**
     * Returns the typed scenario context bound to the current test thread.
     *
     * @return typed scenario context for the current scenario.
     */
    protected ScenarioContext scenarioContext() {
        return ScenarioContextHolder.current();
    }

    /**
     * Builds an authorised JSON request specification for the current scenario user.
     *
     * @return request specification configured for the current scenario user.
     */
    protected static RequestSpecification authorisedJsonRequestForCurrentUser() {
        return buildJsonRequestWithToken(getToken(), false);
    }

    /**
     * Builds a JSON request specification for the supplied bearer token when one is present.
     *
     * @param token bearer token to apply to the request when present.
     * @return request specification configured with the supplied bearer token when one is present.
     */
    protected static RequestSpecification jsonRequestWithOptionalToken(String token) {
        return buildJsonRequestWithToken(token, false);
    }

    /**
     * Builds an authorised JSON request specification for the current scenario.
     *
     * @return request specification configured for the current scenario user.
     */
    protected RequestSpecification authorisedJsonRequest() {
        return authorisedJsonRequestForCurrentUser();
    }

    /**
     * Builds an authorised JSON request specification with full request logging enabled.
     *
     * @return request specification configured for the current scenario user with request logging
     *         enabled.
     */
    protected RequestSpecification loggedAuthorisedJsonRequest() {
        return buildJsonRequestWithToken(getToken(), true);
    }

    /**
     * Builds an authorised JSON request specification for the supplied bearer token.
     *
     * @param token bearer token to apply to the request when present.
     * @return request specification configured with the supplied bearer token.
     */
    protected RequestSpecification jsonRequestWithToken(String token) {
        return jsonRequestWithOptionalToken(token);
    }

    /**
     * Builds the shared JSON request specification used by functional-test steps.
     *
     * @param token bearer token to apply to the request when present.
     * @param logAll whether full request logging should be enabled.
     * @return request specification configured from the supplied token settings.
     */
    private static RequestSpecification buildJsonRequestWithToken(String token, boolean logAll) {
        RequestSpecification request = SerenityRest.given();
        if (logAll) {
            request = request.log().all();
        }

        request = request.accept("*/*").contentType("application/json");
        if (dataExists(token)) {
            request = request.header("Authorization", "Bearer " + token);
        }

        return request;
    }

    /**
     * Resolves the user-service base URL from the configured environment variables.
     *
     * @return resolved user-service base URL.
     */
    private static String resolveUserServiceUrl() {
        String primary = System.getenv("OPAL_USER_SERVICE_API_URL");
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        String fallback = System.getenv("DEV_OPAL_USER_SERVICE_API_URL");
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "http://localhost:4555";
    }

    /**
     * Creates a new JSON object containing the named fields copied from the supplied map.
     *
     * @param dataToPost source values keyed by JSON field name.
     * @param names names of the fields to copy into the new JSON object.
     * @return JSON object populated with the requested fields.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static JSONObject addToNewJsonObject(Map<String, String> dataToPost, String... names) throws JSONException {
        return addAllToJsonObject(new JSONObject(), dataToPost, names);
    }

    /**
     * Adds the named fields from the supplied map to an existing JSON object.
     *
     * @param json JSON object to update.
     * @param dataToPost source values keyed by JSON field name.
     * @param names names of the fields to copy into the JSON object.
     * @return the supplied JSON object after the requested fields have been added.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static JSONObject addAllToJsonObject(JSONObject json, Map<String, String> dataToPost, String... names)
                                                                                            throws JSONException {
        for (String name: names) {
            addToJsonObject(json, dataToPost, name);
        }
        return json;
    }

    /**
     * Adds a string field to the supplied JSON object, defaulting missing values to an empty
     * string.
     *
     * @param json JSON object to update.
     * @param dataToPost source values keyed by JSON field name.
     * @param name name of the field to copy into the JSON object.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static void addToJsonObject(JSONObject json, Map<String, String> dataToPost, String name)
                                                                                            throws JSONException {
        json.put(name, dataToPost.get(name) != null ? dataToPost.get(name) : "");
    }

    /**
     * Checks whether the supplied string contains a non-blank value.
     *
     * @param data string value to check.
     * @return true if the supplied string is non-blank; otherwise false.
     */
    public static boolean dataExists(String data) {
        return data != null && !data.isBlank();
    }

    /**
     * Adds an integer field to the supplied JSON object when the source value is present.
     *
     * @param json JSON object to update.
     * @param dataToPatch source values keyed by JSON field name.
     * @param key name of the integer field to copy into the JSON object.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static void addIntToJsonObject(JSONObject json, Map<String, String> dataToPatch, String key)
        throws JSONException {
        String data = dataToPatch.get(key);
        if (dataExists(data)) {
            json.put(key, Integer.parseInt(data));
        }
    }

    /**
     * Adds a long field to the supplied JSON object when the source value is present.
     *
     * @param json JSON object to update.
     * @param dataToPatch source values keyed by JSON field name.
     * @param key name of the long field to copy into the JSON object.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static void addLongToJsonObject(JSONObject json, Map<String, String> dataToPatch, String key)
        throws JSONException {
        String data = dataToPatch.get(key);
        if (dataExists(data)) {
            json.put(key, Long.parseLong(data));
        }
    }

    /**
     * Adds a string field to the JSON object when the source value is present.
     *
     * @param json JSON object to update.
     * @param dataToPatch source values keyed by JSON field name.
     * @param key name of the field to copy into the JSON object.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static void addIfPresentToJsonObject(JSONObject json, Map<String, String> dataToPatch, String key)
        throws JSONException {
        String data = dataToPatch.get(key);
        if (dataExists(data)) {
            json.put(key, data);
        }
    }

    /**
     * Adds the named field to the JSON object, or `null` when the source value is blank.
     *
     * @param json JSON object to update.
     * @param data source values keyed by JSON field name.
     * @param key name of the field to copy into the JSON object.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static void addToJsonObjectOrNull(JSONObject json, Map<String, String> data,
                                             String key) throws JSONException {
        String value = data.get(key);
        json.put(key, dataExists(value) ? value : JSONObject.NULL); // added here
    }

    /**
     * Creates an HTTP header using the named value from the supplied test data.
     *
     * @param headerName header name to create and to look up in the data map.
     * @param dataToPatch source values keyed by header name.
     * @return header built from the supplied string value.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static Header createStringHeader(String headerName, Map<String, String> dataToPatch)
        throws JSONException {
        return new Header(headerName, dataToPatch.get(headerName));
    }

    /**
     * Creates an HTTP header whose value is normalised as a numeric string.
     *
     * @param headerName header name to create and to look up in the data map.
     * @param dataToPatch source values keyed by header name.
     * @return header built from the supplied numeric value.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static Header createLongHeader(String headerName, Map<String, String> dataToPatch)
        throws JSONException {
        return new Header(headerName, checkLong(dataToPatch.get(headerName)));
    }

    /**
     * Creates an HTTP header whose numeric value is wrapped in quotes.
     *
     * @param headerName header name to create and to look up in the data map.
     * @param dataToPatch source values keyed by header name.
     * @return header built from the supplied numeric value enclosed in quotes.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    public static Header createQuotedLongHeader(String headerName, Map<String, String> dataToPatch)
        throws JSONException {
        return new Header(headerName, "\"" + checkLong(dataToPatch.get(headerName)) + "\"");
    }

    /**
     * Normalises the supplied numeric value as a string.
     *
     * @param candidate numeric string value to normalise.
     * @return numeric string normalised from the supplied value.
     */
    private static String checkLong(String candidate) {
        return String.valueOf(Long.parseLong(candidate));
    }
}
