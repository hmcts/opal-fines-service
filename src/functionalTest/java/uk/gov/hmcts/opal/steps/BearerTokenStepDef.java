package uk.gov.hmcts.opal.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines Cucumber steps and token helpers for authenticated functional-test calls.
 */
public class BearerTokenStepDef extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(BearerTokenStepDef.class.getName());
    private static final String DEFAULT_USER = "opal-test@dev.platform.hmcts.net";
    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<String> ALT_TOKEN = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    /**
     * Clears any scenario-specific alternate bearer token.
     */
    @Before
    public void resetAltToken() {
        clearTokenOverride();
    }

    /**
     * Returns a cached access token for the supplied user, fetching one when needed.
     *
     * @param user user alias or email used to resolve the bearer token.
     * @return access token for the supplied user.
     */
    public String getAccessTokenForUser(String user) {
        return tokenCache.computeIfAbsent(user, BearerTokenStepDef::fetchAccessToken);
    }

    /**
     * Fetches and caches an access token for the supplied user.
     *
     * @param user user alias or email used to resolve the bearer token.
     * @return access token for the supplied user.
     */
    private static String fetchAccessToken(String user) {
        return fetchToken(user);
    }

    /**
     * Calls the user-service test-support endpoint to fetch an access token.
     *
     * @param user user alias or email used to resolve the bearer token.
     * @return access token returned by the user-service test-support endpoint.
     */
    private static String fetchToken(String user) {
        TestHttpResponse response = TestHttpClient.get(
            getUserServiceUrl() + "/testing-support/token/user",
            Map.of(
                "Accept", "*/*",
                "Content-Type", "application/json",
                "X-User-Email", user
            )
        );

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to fetch access token, status: " + response.statusCode());
        }

        return response.jsonPath("access_token");
    }

    /**
     * Returns the bearer token that should be used for the current scenario.
     *
     * @return bearer token that should be used for the current scenario.
     */
    public static String getToken() {
        if (ALT_TOKEN.get() != null) {
            return ALT_TOKEN.get();
        }

        if (TOKEN.get() == null) {
            TOKEN.set(tokenCache.computeIfAbsent(DEFAULT_USER, BearerTokenStepDef::fetchAccessToken));
        }

        return TOKEN.get();
    }

    /**
     * Applies a scenario-scoped bearer-token override for subsequent requests on the current
     * thread.
     *
     * @param token bearer token override to use for subsequent requests.
     */
    public static void setTokenOverride(String token) {
        ALT_TOKEN.set(token);
    }

    /**
     * Clears any scenario-scoped bearer-token override for the current thread.
     */
    public static void clearTokenOverride() {
        ALT_TOKEN.remove();
    }

    /**
     * Uses the access token for the supplied user in the current scenario.
     *
     * @param user user alias or email used to resolve the bearer token.
     */
    @When("I am testing as the {string} user")
    public void setTokenWithUser(String user) {
        setTokenOverride(getAccessTokenForUser(user));
    }

    /**
     * Sets the current scenario to use an invalid bearer token.
     */
    @When("I set an invalid token")
    public void setInvalidToken() {
        setTokenOverride("invalid-token");
    }

    /**
     * Calls the supplied endpoint without an Authorization header.
     *
     * @param method HTTP method to invoke.
     * @param path relative API path to call.
     */
    @When("I call {word} {string} without a token")
    public void callWithoutToken(String method, String path) {
        TestHttpResponse response = TestHttpClient.request(
            method,
            getTestUrl() + path,
            Map.of("Accept", "*/*"),
            null
        );
        scenarioContext().setLatestHttpResponse(response);
    }

    /**
     * Calls the supplied endpoint with an invalid Authorization header.
     *
     * @param method HTTP method to invoke.
     * @param path relative API path to call.
     */
    @When("I call {word} {string} with an invalid token")
    public void callWithInvalidToken(String method, String path) {
        TestHttpResponse response = TestHttpClient.request(
            method,
            getTestUrl() + path,
            Map.of(
                "Accept", "*/*",
                "Authorization", "Bearer invalidToken"
            ),
            null
        );
        scenarioContext().setLatestHttpResponse(response);
    }

    /**
     * Builds a syntactically valid but expired bearer token for negative-auth scenarios.
     *
     * @param user user alias or email used to resolve the bearer token.
     * @return syntactically valid but expired bearer token.
     */
    private static String fetchExpiredToken(String user) {
        // Create a dummy JWT that looks real but has an expired "exp" timestamp
        long now = System.currentTimeMillis() / 1000L;
        long expired = now - 3600; // expired 1 hour ago

        // Build standard JWT header + payload
        String header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        String payloadJson = String.format(
            "{\"sub\":\"%s\",\"email\":\"%s\",\"iat\":%d,\"exp\":%d}",
            user, user, expired - 60, expired
        );
        String payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        // Signature: use a dummy secret, since we just need it to *look* valid
        String signature = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("expired-signature".getBytes(StandardCharsets.UTF_8));

        // Combine header, payload, and signature into a JWT
        String expiredToken = String.format("%s.%s.%s", header, payload, signature);

        // Log what we generated (optional)
        log.info("Generated expired token for user {})", user);

        return expiredToken;
    }

    /**
     * Uses an expired access token for the supplied user in the current scenario.
     *
     * @param user user alias or email used to resolve the bearer token.
     */
    @When("I am testing with an expired token for the {string} user")
    public void setExpiredTokenForUser(String user) {
        setTokenOverride(fetchExpiredToken(user));
    }


    /**
     * Clears the cached bearer tokens after the test run.
     */
    @AfterAll
    public static void clearCache() {
        tokenCache.clear();
        ALT_TOKEN.remove();
        TOKEN.remove();
    }
}
