package uk.gov.hmcts.opal.steps.refdata;

import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.steps.CommonMethods;

import static uk.gov.hmcts.opal.config.Constants.ENFORCERS_REF_DATA_URI;

/**
 * Defines Cucumber steps for enforcer reference-data requests.
 */
public class EnforcerRefDataStepDef {
    private final CommonMethods methods = new CommonMethods();

    /**
     * Retrieves enforcer reference data filtered by enforcer name.
     *
     * @param enforcerName enforcer name to append to the request path.
     */
    @When("I make a request to enforcer ref data api filtering by name {string}")
    public void getRequestToEnforcerRefDataByName(String enforcerName) {
        methods.getRequest(ENFORCERS_REF_DATA_URI + enforcerName);
    }
}
