package uk.gov.hmcts.opal.steps.refdata;

import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.steps.CommonMethods;

import static uk.gov.hmcts.opal.config.Constants.BUSINESS_UNIT_REF_DATA_URI;

/**
 * Defines Cucumber steps for business-unit reference-data requests.
 */
public class BusinessUnitRefDataStepDef {
    private final CommonMethods methods = new CommonMethods();

    /**
     * Retrieves business-unit reference data filtered by business-unit type.
     *
     * @param filter business-unit type filter to apply to the request.
     */
    @When("I make a request to the business unit ref data api filtering by business unit type {string}")
    public void getRequestToBusinessUnitRefData(String filter) {
        methods.getRequest(BUSINESS_UNIT_REF_DATA_URI + "?q=" + filter);
    }

    /**
     * Retrieves business-unit reference data with the raw HTTP client to exercise the same
     * business-unit-type filter path.
     *
     * @param filter business-unit type filter to apply to the request.
     */
    @When("I make a raw request to the business unit ref data api filtering by business unit type {string}")
    public void getRawRequestToBusinessUnitRefData(String filter) {
        methods.getRequestUsingRawHttpClient(BUSINESS_UNIT_REF_DATA_URI + "?q=" + filter);
    }
}
