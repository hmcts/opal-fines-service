package uk.gov.hmcts.opal.utils.draftaccount;

import io.restassured.response.Response;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

/**
 * Provides reusable draft-account ETag helpers for assertions and concurrency control.
 */
public final class DraftAccountEtagHelper extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DraftAccountEtagHelper.class);
    private static final Pattern STRONG_ETAG = Pattern.compile("^\"[^\"]+\"$");

    private DraftAccountEtagHelper() {
        // Utility class.
    }

    /**
     * Determines whether an ETag value is strong and quoted.
     *
     * @param etag ETag value to validate.
     * @return true if the ETag is strong and quoted; otherwise false.
     */
    public static boolean isStrongEtag(String etag) {
        return etag != null && STRONG_ETAG.matcher(etag).matches();
    }

    /**
     * Fetches the current strong ETag for a draft account by issuing a GET to
     * {@code /draft-accounts/{id}}.
     *
     * @param baseUrl base URL for the API under test.
     * @param id draft-account identifier to fetch.
     * @return strong quoted ETag for the draft account, or null when the draft account does not
     *         exist.
     */
    public static String fetchStrongEtag(String baseUrl, String id) {
        return fetchStrongEtag(baseUrl, id, null);
    }

    /**
     * Fetches the current strong ETag for a draft account by issuing a GET to
     * {@code /draft-accounts/{id}} using the supplied bearer token when one is provided.
     *
     * @param baseUrl base URL for the API under test.
     * @param id draft-account identifier to fetch.
     * @param token bearer token to use for the request, or {@code null} to use the current
     *              scenario user.
     * @return strong quoted ETag for the draft account, or null when the draft account does not
     *         exist.
     */
    public static String fetchStrongEtag(String baseUrl, String id, String token) {
        Response response = (
            token == null ? authorisedJsonRequestForCurrentUser() : jsonRequestWithOptionalToken(token)
        )
            .accept("application/json")
            .get(baseUrl + DRAFT_ACCOUNTS_URI + "/" + id);

        int code = response.getStatusCode();
        if (code == 404) {
            log.info("GET {}{} → 404 (no ETag)", baseUrl, DRAFT_ACCOUNTS_URI + "/" + id);
            return null;
        }

        assertThat("GET for ETag should be 200 or 304", code, anyOf(is(200), is(304)));

        String etag = response.getHeader("ETag");
        assertThat("ETag must be present", etag, notNullValue());
        assertThat("ETag must be strong and quoted (e.g., \"42\")", isStrongEtag(etag), is(true));
        return etag;
    }
}
