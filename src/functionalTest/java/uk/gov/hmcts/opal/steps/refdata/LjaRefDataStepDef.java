package uk.gov.hmcts.opal.steps.refdata;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.steps.CommonMethods;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.hmcts.opal.config.Constants.LJA_REF_DATA_URI;

/**
 * Defines Cucumber steps for local justice area reference-data requests and assertions.
 */
public class LjaRefDataStepDef {
    private final CommonMethods methods = new CommonMethods();

    /**
     * Sends a request to the LJA reference-data API.
     */
    @When("I make a request to the LJA ref data api with")
    public void getRequestToLjaRefData() {
        methods.getRequest(LJA_REF_DATA_URI);
    }

    /**
     * Retrieves LJA reference data filtered by one or more `lja_type` values.
     *
     * @param ljaTypeOrCsv single `lja_type` value or comma-separated list of allowed values.
     */
    @When("I make a request to the LJA ref data api with lja_type {string}")
    public void getRequestToLjaRefDataWithLjaType(String ljaTypeOrCsv) {
        methods.getRequest(LJA_REF_DATA_URI + "?lja_type=" + ljaTypeOrCsv);
    }

    /**
     * Asserts that every returned LJA has the expected `lja_type` value.
     *
     * @param expectedType expected `lja_type` value.
     */
    @Then("all returned LJAs have lja_type {string}")
    public void allReturnedLjasHaveLjaType(String expectedType) {
        List<String> types = then().extract().jsonPath().getList("refData.lja_type");

        assertThat("Expected at least one result", types, not(empty()));
        assertThat(types, everyItem(equalTo(expectedType)));
    }

    /**
     * Asserts that every returned LJA has an `lja_type` contained in the supplied allow-list.
     *
     * @param allowedTypesCsv comma-separated list of allowed `lja_type` values.
     */
    @Then("all returned LJAs have lja_type in {string}")
    public void allReturnedLjasHaveTypeIn(String allowedTypesCsv) {
        Set<String> allowed = Arrays.stream(allowedTypesCsv.split(","))
            .map(String::trim)
            .collect(java.util.stream.Collectors.toSet());

        List<String> types = then().extract().jsonPath().getList("refData.lja_type");

        assertThat("Expected at least one result", types, not(empty()));
        boolean allAllowed = allowed.containsAll(types);
        assertThat(
            "Found lja_type not in allowed set. Allowed=" + allowed + ", Actual=" + types,
            allAllowed,
            is(true)
        );
    }
}
