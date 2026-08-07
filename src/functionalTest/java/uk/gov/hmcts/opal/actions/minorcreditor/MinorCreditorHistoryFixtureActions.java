package uk.gov.hmcts.opal.actions.minorcreditor;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Encapsulates the minor-creditor history fixture test-support API used by functional tests.
 */
public class MinorCreditorHistoryFixtureActions extends BaseStepDef {

    private static final String HISTORY_FIXTURE_PATH = "/testing-support/minor-creditor-history";

    /**
     * Creates a self-contained minor-creditor account with representative history.
     *
     * @param reference scenario identifier used in visible fixture data.
     * @return API response returned by the fixture endpoint.
     * @throws JSONException if the fixture request cannot be created.
     */
    public Response createFixture(String reference) throws JSONException {
        return SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .body(new JSONObject().put("reference", reference).toString())
            .when()
            .post(getTestUrl() + HISTORY_FIXTURE_PATH);
    }

    /**
     * Deletes a fixture created by {@link #createFixture(String)}.
     *
     * @param creditorAccountId creditor account id returned by the create response.
     * @return API response returned by the delete request.
     */
    public Response deleteFixture(long creditorAccountId) {
        return SerenityRest.given()
            .accept("*/*")
            .when()
            .delete(getTestUrl() + HISTORY_FIXTURE_PATH + "/" + creditorAccountId);
    }
}
