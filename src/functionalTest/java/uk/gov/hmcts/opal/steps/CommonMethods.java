package uk.gov.hmcts.opal.steps;

import uk.gov.hmcts.opal.utils.TestHttpClient;

import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

/**
 * Provides reusable HTTP helpers for functional-test step definitions.
 */
public class CommonMethods extends BaseStepDef {

    /**
     * Executes an authorised GET request against the supplied reference-data path.
     *
     * @param refDataUri reference-data URI to request.
     */
    public void getRequest(String refDataUri) {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + refDataUri);
    }

    /**
     * Executes a raw authorised GET request against the supplied reference-data path.
     *
     * @param refDataUri reference-data URI to request.
     */
    public void getRequestUsingRawHttpClient(String refDataUri) {
        scenarioContext().setLatestHttpResponse(
            TestHttpClient.request(
                "GET",
                getTestUrl() + refDataUri,
                java.util.Map.of(
                    "Accept", "*/*",
                    "Authorization", "Bearer " + getToken(),
                    "Content-Type", "application/json"
                ),
                null
            )
        );
    }

}
