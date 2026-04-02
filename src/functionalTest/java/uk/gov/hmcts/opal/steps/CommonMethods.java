package uk.gov.hmcts.opal.steps;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.utils.TestHttpClient;

import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class CommonMethods extends BaseStepDef {

    public void getRequest(String refDataUri) {
        SerenityRest
            .given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + refDataUri);
    }

    public void getRequestUsingRawHttpClient(String refDataUri) {
        Serenity.setSessionVariable(LATEST_HTTP_RESPONSE).to(
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
