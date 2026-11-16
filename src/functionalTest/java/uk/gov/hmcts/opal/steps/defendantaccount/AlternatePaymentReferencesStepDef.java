package uk.gov.hmcts.opal.steps.defendantaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

/** Deployed-environment functional coverage for alternate payment reference endpoints. */
public class AlternatePaymentReferencesStepDef extends BaseStepDef {

    private static final String COLLECTION_PATH = "/defendant-accounts/%s/alternate-payment-references";
    private static final String NON_EXISTENT_ACCOUNT_ID = "90000000000000";
    private static final String TOO_LONG_APR = "APR-1234567890123456";
    private static final long TIMESTAMP_PRECISION_TOLERANCE_NANOS = 1_000;
    private static final long TIMESTAMP_CLOCK_SKEW_TOLERANCE_SECONDS = 30;

    private Instant scenarioStartedAt;
    private String createdAprText;
    private String createdAprId;
    private String createdAprCreatedAt;
    private String createdAprUpdatedAt;
    private String expectedAprText;
    private String fixtureAccountId;
    private String fixtureAprId;
    private String fixtureEtag;
    private String fixtureAprText;
    private String fixtureRelationship;
    private String fixtureCategory;
    private final DefendantAccountEnforcementWorkflow accountWorkflow = new DefendantAccountEnforcementWorkflow();

    @Before
    public void recordScenarioStartTime() {
        scenarioStartedAt = Instant.now();
    }

    @Given("a defendant account exists in business unit {string}")
    public void createAprTestDefendantAccount(String businessUnitId) throws JSONException, IOException {
        createDefendantAccountForAprTest(businessUnitId);
    }

    @Given("a defendant account {string} exists in business unit {string}")
    public void createNamedAprTestDefendantAccount(String accountAlias, String businessUnitId)
        throws JSONException, IOException {
        createDefendantAccountForAprTest(businessUnitId);
        scenarioContext().setFeatureVariable(accountAlias, latestCreatedAccountId());
    }

    private void createDefendantAccountForAprTest(String businessUnitId) throws JSONException, IOException {
        accountWorkflow.createEnforceableDefendantAccount(Map.of(
            "source", "MAC",
            "business_unit_id", businessUnitId,
            "account", "draftAccounts/accountJson/adultAccount.json",
            "account_type", "Fine",
            "account_status", "Submitted",
            "submitted_by", "APR" + Long.toString(System.nanoTime(), 36).substring(0, 5).toUpperCase(),
            "submitted_by_name", "APR Test"
        ));
    }

    @Given("a disposable APR fixture exists for relationship {string} and category {string} "
        + "on the created defendant account")
    public void createAprFixtureOnCreatedDefendantAccount(String relationship, String category) {
        fixtureAccountId = createdAccountId();
        String normalizedRelationship = relationship.toUpperCase(Locale.ROOT);
        String normalizedCategory = category.toUpperCase(Locale.ROOT);
        String aprText = uniqueAprText();
        try (AlternatePaymentReferenceFixtureDatabaseClient database =
                 new AlternatePaymentReferenceFixtureDatabaseClient()) {
            fixtureAprId = String.valueOf(database.createFixture(
                fixtureAccountId, normalizedRelationship, normalizedCategory, aprText));
        }
        captureAprFixture(normalizedRelationship, normalizedCategory);
    }

    @After(value = "@JIRA-LABEL:apr-management", order = Integer.MAX_VALUE - 1)
    public void cleanUpAprsForCreatedDefendantAccounts() {
        List<String> defendantAccountIds = new ArrayList<>(scenarioContext().getCreatedDefendantAccountIds());
        if (fixtureAccountId != null && !defendantAccountIds.contains(fixtureAccountId)) {
            defendantAccountIds.add(fixtureAccountId);
        }

        try {
            if (!defendantAccountIds.isEmpty()) {
                try (AlternatePaymentReferenceFixtureDatabaseClient database =
                         new AlternatePaymentReferenceFixtureDatabaseClient()) {
                    for (String defendantAccountId : defendantAccountIds) {
                        database.deleteForDefendantAccount(defendantAccountId);
                    }
                }
            }
        } finally {
            fixtureAccountId = null;
            fixtureAprId = null;
        }
    }

    private void captureAprFixture(String normalizedRelationship, String normalizedCategory) {
        Response response = getCollection(fixtureAccountId);
        assertEquals(200, response.statusCode(), "Could not retrieve the deployed APR fixture");
        List<String> relationships = response.jsonPath().getList("relationship");
        List<String> categories = response.jsonPath().getList("category");
        List<String> ids = aprIds(response);
        int matchIndex = -1;
        if (relationships != null) {
            for (int index = 0; index < relationships.size(); index++) {
                if (normalizedRelationship.equals(relationships.get(index))
                    && (normalizedCategory == null || normalizedCategory.equals(categories.get(index)))) {
                    matchIndex = index;
                    break;
                }
            }
        }
        assertTrue(matchIndex >= 0, "Fixture account has no " + normalizedRelationship + " APR row");
        fixtureAprId = ids.get(matchIndex);
        fixtureRelationship = relationships.get(matchIndex);
        fixtureCategory = categories.get(matchIndex);
        fixtureAprText = response.jsonPath().getList("apr_text", String.class).get(matchIndex);
        fixtureEtag = requireEtag(response);
    }

    @When("I delete the prepared deployed APR fixture")
    public void deletePreparedDeployedAprFixture() {
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, fixtureEtag)
            .when().delete(getTestUrl() + itemPath(fixtureAccountId, fixtureAprId));
    }

    @Then("the prepared deployed APR fixture is deleted")
    public void preparedDeployedAprFixtureIsDeleted() {
        Response deleteResponse = SerenityRest.lastResponse();
        assertEquals(200, deleteResponse.statusCode(), "Unexpected APR fixture deletion status");
        assertEquals(fixtureAprId, deleteResponse.jsonPath().getString("alternate_payment_reference_id"));
        Response latest = getCollection(fixtureAccountId);
        assertEquals(200, latest.statusCode(), "Could not verify deletion of the deployed APR fixture");
        assertFalse(aprIds(latest).contains(fixtureAprId), "Deleted APR fixture remains on the defendant account");
    }

    @Then("the prepared deployed APR fixture delete is rejected with status {int} and remains present")
    public void preparedDeployedAprFixtureDeleteIsRejected(int expectedStatus) {
        assertEquals(expectedStatus, SerenityRest.lastResponse().statusCode(),
            "Unexpected read-only APR delete status");
        Response latest = getCollection(fixtureAccountId);
        assertEquals(200, latest.statusCode(), "Could not verify the rejected APR fixture deletion");
        int index = aprIds(latest).indexOf(fixtureAprId);
        assertTrue(index >= 0, "Rejected APR fixture was not returned by GET");
        assertEquals(fixtureAprText, latest.jsonPath().getList("apr_text", String.class).get(index));
        assertEquals(fixtureRelationship, latest.jsonPath().getList("relationship", String.class).get(index));
        assertEquals(fixtureCategory, latest.jsonPath().getList("category", String.class).get(index));
    }

    @When("I update the prepared deployed APR fixture")
    public void updatePreparedDeployedAprFixture() {
        expectedAprText = uniqueAprText();
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, fixtureEtag)
            .body("{\"apr_text\":\"" + expectedAprText + "\"}")
            .when().patch(getTestUrl() + itemPath(fixtureAccountId, fixtureAprId));
    }

    @Then("the prepared deployed APR fixture is updated and retains its relationship and category")
    public void preparedDeployedAprFixtureIsUpdated() {
        Response updateResponse = SerenityRest.lastResponse();
        assertEquals(200, updateResponse.statusCode(), "Unexpected inherited APR update status");
        assertEquals(expectedAprText, updateResponse.jsonPath().getString("apr_text"));
        assertEquals(fixtureAprId, updateResponse.jsonPath().getString("alternate_payment_reference_id"));
        assertEquals(fixtureAccountId, updateResponse.jsonPath().getString("defendant_account_id"));
        assertEquals(fixtureRelationship, updateResponse.jsonPath().getString("relationship"));
        assertEquals(fixtureCategory, updateResponse.jsonPath().getString("category"));
        assertNotNull(updateResponse.getHeader(HttpHeaders.ETAG), "Expected the updated account ETag");

        Response latest = getCollection(fixtureAccountId);
        assertEquals(200, latest.statusCode(), "Could not verify the updated APR fixture");
        int index = aprIds(latest).indexOf(fixtureAprId);
        assertTrue(index >= 0, "Updated APR fixture was not returned by GET");
        assertEquals(expectedAprText, latest.jsonPath().getList("apr_text", String.class).get(index));
        assertEquals(fixtureRelationship, latest.jsonPath().getList("relationship", String.class).get(index));
        assertEquals(fixtureCategory, latest.jsonPath().getList("category", String.class).get(index));
        assertEquals(fixtureAccountId,
            String.valueOf(latest.jsonPath().getList("defendant_account_id").get(index)));
    }

    @Then("the prepared deployed APR fixture update is rejected with status {int} and its text is unchanged")
    public void preparedDeployedAprFixtureUpdateIsRejected(int expectedStatus) {
        assertEquals(expectedStatus, SerenityRest.lastResponse().statusCode(), "Unexpected read-only APR status");
        Response latest = getCollection(fixtureAccountId);
        assertEquals(200, latest.statusCode(), "Could not verify the rejected APR fixture update");
        int index = aprIds(latest).indexOf(fixtureAprId);
        assertTrue(index >= 0, "Rejected APR fixture was not returned by GET");
        assertEquals(fixtureAprText, latest.jsonPath().getList("apr_text", String.class).get(index));
        assertEquals(fixtureRelationship, latest.jsonPath().getList("relationship", String.class).get(index));
        assertEquals(fixtureCategory, latest.jsonPath().getList("category", String.class).get(index));
    }

    @When("I retrieve alternate payment references for the created defendant account")
    public void retrieveAlternatePaymentReferencesForCreatedAccount() {
        getCollection(createdAccountId());
    }

    @When("I retrieve alternate payment references for a non-existent defendant account")
    public void retrieveAlternatePaymentReferencesForMissingAccount() {
        getCollection(NON_EXISTENT_ACCOUNT_ID);
    }

    @When("I retrieve alternate payment references for the created defendant account as {string}")
    public void retrieveAlternatePaymentReferencesAsUser(String email) {
        jsonRequestWithToken(uk.gov.hmcts.opal.steps.BearerTokenStepDef.getAccessTokenForUser(email))
            .when().get(getTestUrl() + collectionPath(createdAccountId()));
    }

    @When("I create an alternate payment reference for the created defendant account")
    public void createAlternatePaymentReference() {
        createAlternatePaymentReferenceForAccount(createdAccountId());
    }

    @When("I create an alternate payment reference for defendant account {string}")
    public void createAlternatePaymentReferenceForNamedAccount(String accountAlias) {
        createAlternatePaymentReferenceForAccount(defendantAccountId(accountAlias));
    }

    private void createAlternatePaymentReferenceForAccount(String accountId) {
        createdAprText = uniqueAprText();
        expectedAprText = createdAprText;
        scenarioContext().setFeatureVariable("createdAprText", createdAprText);
        Response current = getCollection(accountId);
        String etag = requireEtag(current);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, etag)
            .body("{\"apr_text\":\"" + createdAprText + "\"}")
            .when().post(getTestUrl() + collectionPath(accountId));
    }

    @When("I create another alternate payment reference for defendant account {string}")
    public void createAnotherAlternatePaymentReferenceForNamedAccount(String accountAlias) {
        String accountId = defendantAccountId(accountAlias);
        Response current = getCollection(accountId);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .body("{\"apr_text\":\"" + uniqueAprText() + "\"}")
            .when().post(getTestUrl() + collectionPath(accountId));
    }

    @Given("an alternate payment reference exists for the created defendant account")
    public void alternatePaymentReferenceExistsForCreatedAccount() {
        createAlternatePaymentReference();
        assertEquals(201, SerenityRest.lastResponse().statusCode(), "Could not set up the defendant account APR");
        assertExpectedAlternatePaymentReferenceResponse();
    }

    @When("I create an alternate payment reference with invalid length")
    public void createAlternatePaymentReferenceWithInvalidLength() {
        Response current = getCollection(createdAccountId());
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .body("{\"apr_text\":\"" + TOO_LONG_APR + "\"}")
            .when().post(getTestUrl() + collectionPath(createdAccountId()));
    }

    @When("I create an alternate payment reference with the same name on another created defendant account")
    public void createDuplicateAprOnAnotherAccount() {
        String firstAccountId = createdAccountId();
        String otherAccountId = latestCreatedAccountId();
        assertFalse(firstAccountId.equals(otherAccountId), "Expected two different defendant accounts");
        createAprWithExistingText(otherAccountId);
    }

    @When("I create an alternate payment reference with the same name on defendant account {string}")
    public void createDuplicateAprOnNamedAccount(String accountAlias) {
        createAprWithExistingText(defendantAccountId(accountAlias));
    }

    private void createAprWithExistingText(String accountId) {
        Response current = getCollection(accountId);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .body("{\"apr_text\":\"" + createdAprText + "\"}")
            .when().post(getTestUrl() + collectionPath(accountId));
    }

    @When("I create a different alternate payment reference on another created defendant account")
    public void createDifferentAprOnAnotherAccount() {
        String accountId = latestCreatedAccountId();
        createDifferentApr(accountId);
    }

    @When("I create a different alternate payment reference on defendant account {string}")
    public void createDifferentAprOnNamedAccount(String accountAlias) {
        createDifferentApr(defendantAccountId(accountAlias));
    }

    private void createDifferentApr(String accountId) {
        Response current = getCollection(accountId);
        String differentAprText = uniqueAprText();
        scenarioContext().setFeatureVariable("otherAprText", differentAprText);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .body("{\"apr_text\":\"" + differentAprText + "\"}")
            .when().post(getTestUrl() + collectionPath(accountId));
    }

    @Given("an alternate payment reference exists for defendant account {string}")
    public void alternatePaymentReferenceExistsForNamedAccount(String accountAlias) {
        createAlternatePaymentReferenceForAccount(defendantAccountId(accountAlias));
        assertEquals(201, SerenityRest.lastResponse().statusCode(),
            "Could not create the alternate payment reference for defendant account " + accountAlias);
        assertExpectedAlternatePaymentReferenceResponse();
        String aprText = SerenityRest.lastResponse().jsonPath().getString("apr_text");
        scenarioContext().setFeatureVariable("aprText_" + accountAlias, aprText);
    }

    @When("I create the same alternate payment reference on another created defendant account")
    public void createSameAprOnAnotherAccount() {
        String accountId = latestCreatedAccountId();
        createSameApr(accountId);
    }

    @When("I create the same alternate payment reference on defendant account {string}")
    public void createSameAprOnNamedAccount(String accountAlias) {
        createSameApr(defendantAccountId(accountAlias));
    }

    private void createSameApr(String accountId) {
        Response current = getCollection(accountId);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .body("{\"apr_text\":\"" + createdAprText + "\"}")
            .when().post(getTestUrl() + collectionPath(accountId));
    }

    @When("I update the created alternate payment reference")
    public void updateCreatedAlternatePaymentReference() {
        Response current = getCollection(createdAccountId());
        rememberApr(current);
        expectedAprText = uniqueAprText();
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .body("{\"apr_text\":\"" + expectedAprText + "\"}")
            .when().patch(getTestUrl() + itemPath(createdAccountId(), createdAprId));
    }

    @When("I update the created alternate payment reference with an outdated version")
    public void updateCreatedAlternatePaymentReferenceWithOutdatedVersion() {
        Response current = getCollection(createdAccountId());
        rememberApr(current);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, "\"999999999999\"")
            .body("{\"apr_text\":\"" + uniqueAprText() + "\"}")
            .when().patch(getTestUrl() + itemPath(createdAccountId(), createdAprId));
    }

    @When("I update the created alternate payment reference with invalid length")
    public void updateAlternatePaymentReferenceWithInvalidLength() {
        Response current = getCollection(createdAccountId());
        rememberApr(current);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .body("{\"apr_text\":\"" + TOO_LONG_APR + "\"}")
            .when().patch(getTestUrl() + itemPath(createdAccountId(), createdAprId));
    }

    @When("I update the created alternate payment reference to the other account's name")
    public void updateAprToOtherAccountName() {
        updateAprToAccountName(latestCreatedAccountId());
    }

    @When("I update the created alternate payment reference to defendant account {string} name")
    public void updateAprToNamedAccountName(String accountAlias) {
        updateAprToAccountName(defendantAccountId(accountAlias));
    }

    @When("I update defendant account {string} APR to the name on defendant account {string}")
    public void updateAprBetweenNamedAccounts(String targetAccountAlias, String sourceAccountAlias) {
        updateAprToAccountName(defendantAccountId(targetAccountAlias), defendantAccountId(sourceAccountAlias));
    }

    private void updateAprToAccountName(String otherAccountId) {
        updateAprToAccountName(createdAccountId(), otherAccountId);
    }

    private void updateAprToAccountName(String accountId, String otherAccountId) {
        Response own = getCollection(accountId);
        rememberApr(own);
        Response other = getCollection(otherAccountId);
        List<String> values = other.jsonPath().getList("apr_text");
        assertTrue(values != null && !values.isEmpty(), "Expected an APR on the other account");
        expectedAprText = values.get(0);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(own))
            .body("{\"apr_text\":\"" + expectedAprText + "\"}")
            .when().patch(getTestUrl() + itemPath(accountId, createdAprId));
    }

    @When("I delete the created alternate payment reference")
    public void deleteCreatedAlternatePaymentReference() {
        Response current = getCollection(createdAccountId());
        rememberApr(current);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, requireEtag(current))
            .when().delete(getTestUrl() + itemPath(createdAccountId(), createdAprId));
    }

    @When("I delete the created alternate payment reference with an outdated version")
    public void deleteAprWithOutdatedVersion() {
        Response current = getCollection(createdAccountId());
        rememberApr(current);
        authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, "\"999999999999\"")
            .when().delete(getTestUrl() + itemPath(createdAccountId(), createdAprId));
    }

    @Then("the alternate payment references response is an empty array")
    public void alternatePaymentReferencesAreEmpty() {
        assertEquals(0, SerenityRest.lastResponse().jsonPath().getList("$").size());
    }

    @Then("the alternate payment references response is as expected")
    public void alternatePaymentReferencesResponseIsAsExpected() {
        Response response = SerenityRest.lastResponse();
        List<String> ids = aprIds(response);
        int createdAprIndex = ids.indexOf(createdAprId);
        assertTrue(createdAprIndex >= 0, "Expected the created APR id in the response: " + ids);

        assertEquals(createdAprText,
            response.jsonPath().getList("apr_text", String.class).get(createdAprIndex));
        assertEquals(createdAccountId(), String.valueOf(
            response.jsonPath().getList("defendant_account_id").get(createdAprIndex)));
        assertEquals("APR", response.jsonPath().getList("relationship", String.class).get(createdAprIndex));
        assertEquals("APR", response.jsonPath().getList("category", String.class).get(createdAprIndex));
        assertEquals("77", String.valueOf(
            response.jsonPath().getList("business_unit_id").get(createdAprIndex)));
        assertTimestampMatchesAtDatabasePrecision(createdAprCreatedAt,
            response.jsonPath().getList("created_at", String.class).get(createdAprIndex), "created_at");
        assertTimestampMatchesAtDatabasePrecision(createdAprUpdatedAt,
            response.jsonPath().getList("updated_at", String.class).get(createdAprIndex), "updated_at");
        String etag = response.getHeader(HttpHeaders.ETAG);
        assertFalse(etag == null || etag.isBlank(), "Expected an ETag on the alternate payment references response");
    }

    @Then("the POST response contains the expected alternate payment reference")
    public void postResponseContainsExpectedAlternatePaymentReference() {
        assertExpectedAlternatePaymentReferenceResponse();
    }

    @Then("the PATCH response contains the expected alternate payment reference")
    public void patchResponseContainsExpectedAlternatePaymentReference() {
        assertExpectedAlternatePaymentReferenceResponse();
    }

    private void assertExpectedAlternatePaymentReferenceResponse() {
        Response response = SerenityRest.lastResponse();
        assertNotNull(response.getHeader(HttpHeaders.ETAG), "Expected an ETag on the response");
        assertEquals(expectedAprText, response.jsonPath().getString("apr_text"));
        createdAprId = response.jsonPath().getString("alternate_payment_reference_id");
        assertFalse(createdAprId == null || createdAprId.isBlank(), "Expected an APR id in the response");
        createdAprCreatedAt = response.jsonPath().getString("created_at");
        createdAprUpdatedAt = response.jsonPath().getString("updated_at");
        assertFalse(createdAprCreatedAt == null || createdAprCreatedAt.isBlank(),
            "Expected created_at in the alternate payment reference response");
        assertFalse(createdAprUpdatedAt == null || createdAprUpdatedAt.isBlank(),
            "Expected updated_at in the alternate payment reference response");
        assertTimestampFallsWithinScenario(createdAprCreatedAt, "created_at");
        assertTimestampFallsWithinScenario(createdAprUpdatedAt, "updated_at");
        assertTrue(!parseUtcTimestamp(createdAprUpdatedAt).isBefore(parseUtcTimestamp(createdAprCreatedAt)),
            "Expected updated_at to be the same time as or later than created_at");
    }

    @Then("defendant account {string} has no alternate payment reference")
    public void defendantAccountHasNoAlternatePaymentReference(String accountAlias) {
        Response latest = getCollection(defendantAccountId(accountAlias));
        assertEquals(200, latest.statusCode(), "Could not verify the defendant account APR collection");
        assertEquals(0, latest.jsonPath().getList("$").size(), "Rejected request must not create an APR");
    }

    @Then("defendant account {string} still has its existing alternate payment reference")
    public void defendantAccountStillHasExistingAlternatePaymentReference(String accountAlias) {
        Response latest = getCollection(defendantAccountId(accountAlias));
        assertEquals(200, latest.statusCode(), "Could not verify the existing APR after the rejected request");
        List<String> ids = aprIds(latest);
        List<String> texts = latest.jsonPath().getList("apr_text");
        assertEquals(1, ids.size(), "Rejected request must not add another APR");
        assertEquals(createdAprId, ids.get(0), "Rejected request must preserve the existing APR");
        assertEquals(createdAprText, texts.get(0), "Rejected request must preserve the existing APR text");
    }

    @Then("the alternate payment reference is absent from the defendant account")
    public void alternatePaymentReferenceIsAbsent() {
        Response latest = getCollection(createdAccountId());
        List<String> ids = aprIds(latest);
        assertFalse(ids.contains(createdAprId), "Deleted APR must be absent from the account");
    }

    private Response getCollection(String accountId) {
        return authorisedJsonRequest().when().get(getTestUrl() + collectionPath(accountId));
    }

    private String requireEtag(Response response) {
        assertEquals(200, response.statusCode(), "Could not retrieve the defendant account APR version");
        String etag = response.getHeader(HttpHeaders.ETAG);
        assertFalse(etag == null || etag.isBlank(), "Expected an ETag from APR retrieval");
        return etag;
    }

    private void rememberApr(Response response) {
        assertEquals(200, response.statusCode(), "Could not retrieve APR to update or delete");
        List<String> ids = aprIds(response);
        List<String> texts = response.jsonPath().getList("apr_text");
        assertFalse(ids == null || ids.isEmpty(), "Expected an APR to update or delete");
        createdAprId = ids.get(0);
        createdAprText = texts.get(0);
    }

    private List<String> aprIds(Response response) {
        List<?> ids = response.jsonPath().getList("alternate_payment_reference_id");
        return ids == null ? List.of() : ids.stream().map(String::valueOf).toList();
    }

    private String createdAccountId() {
        List<String> createdIds = scenarioContext().getCreatedDefendantAccountIds();
        return createdIds.get(0);
    }

    private String latestCreatedAccountId() {
        return scenarioContext().getCreatedDefendantAccountIdOrFail();
    }

    private String defendantAccountId(String accountAlias) {
        return scenarioContext().getFeatureVariableOrFail(accountAlias);
    }

    private String collectionPath(String accountId) {
        return COLLECTION_PATH.formatted(accountId);
    }

    private String itemPath(String accountId, String alternatePaymentReferenceId) {
        return collectionPath(accountId) + "/" + alternatePaymentReferenceId;
    }

    private String uniqueAprText() {
        return "E2E" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private void assertTimestampFallsWithinScenario(String timestamp, String fieldName) {
        Instant value = parseUtcTimestamp(timestamp);
        Instant earliestAllowed = scenarioStartedAt.minusSeconds(TIMESTAMP_CLOCK_SKEW_TOLERANCE_SECONDS);
        Instant latestAllowed = Instant.now().plusSeconds(TIMESTAMP_CLOCK_SKEW_TOLERANCE_SECONDS);
        assertFalse(value.isBefore(earliestAllowed),
            "Expected " + fieldName + " to be on or after scenario start: " + timestamp);
        assertFalse(value.isAfter(latestAllowed),
            "Expected " + fieldName + " not to be in the future: " + timestamp);
    }

    private void assertTimestampMatchesAtDatabasePrecision(String expected, String actual, String fieldName) {
        long differenceNanos = Math.abs(Duration.between(
            parseUtcTimestamp(expected), parseUtcTimestamp(actual)).toNanos());
        assertTrue(differenceNanos <= TIMESTAMP_PRECISION_TOLERANCE_NANOS,
            "Expected GET " + fieldName + " to match POST within database timestamp precision; difference was "
                + differenceNanos + " nanoseconds");
    }

    private Instant parseUtcTimestamp(String timestamp) {
        return LocalDateTime.parse(timestamp).toInstant(ZoneOffset.UTC);
    }
}
