package uk.gov.hmcts.opal.steps.refdata;

import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;

/**
 * Defines shared reference-data summary assertions reused across multiple reference-data APIs.
 */
public class RefDataSummaryStepDef {
    private static final Logger log = LoggerFactory.getLogger(RefDataSummaryStepDef.class);

    /**
     * Asserts that the response count matches the number of returned reference-data records.
     */
    @Then("the LJA ref data matching to result")
    @Then("the court ref data matching to result")
    @Then("the offence ref data matching to result")
    @Then("the enforcer ref data matching to result")
    @Then("the business unit ref data matching to result")
    public void theRefDataMatchingToResult() {
        int totalCount = then().extract().jsonPath().getInt("count");
        int refDataList = then().extract().jsonPath().getList("refData").size();
        log.info("total count is : {}", totalCount);
        log.info("Total records in the json response {}", refDataList);
        then().assertThat().statusCode(200).body("count", equalTo(refDataList));
    }
}
