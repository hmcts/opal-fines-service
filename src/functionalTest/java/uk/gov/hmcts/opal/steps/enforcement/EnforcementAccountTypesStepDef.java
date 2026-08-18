package uk.gov.hmcts.opal.steps.enforcement;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for the enforcement account types endpoint.
 */
public class EnforcementAccountTypesStepDef extends BaseStepDef {

    private static final String ENFORCEMENT_ACCOUNT_TYPES_PATH = "/enforcement-accounts-types";
    private static final String ENFORCEMENT_ACCOUNT_TYPES = "enforcement_account_types";
    private static final long NON_EXISTENT_ENFORCEMENT_ACCOUNT_TYPE_ID = Long.MAX_VALUE;
    private static final Set<String> TOP_LEVEL_FIELDS = Set.of(ENFORCEMENT_ACCOUNT_TYPES);
    private static final Set<String> ENFORCEMENT_ACCOUNT_TYPE_FIELDS = Set.of(
        "id",
        "version",
        "minimum_balance",
        "account_type",
        "enforcement_account_type",
        "path"
    );
    private static final Set<String> EXPECTED_ENFORCEMENT_ACCOUNT_TYPES = Set.of(
        "AL",
        "AH",
        "COL",
        "COH",
        "COLL",
        "COLH",
        "YL",
        "YH"
    );

    private final Map<String, EnforcementAccountTypeSnapshot> originalAccountTypes = new LinkedHashMap<>();
    private List<EnforcementAccountTypeUpdate> submittedUpdates = List.of();
    private Response updateResponse;
    private String snapshotToken;

    /**
     * Restores minimum balances changed by the tagged PATCH scenario. An originally null low-path
     * balance is restored as zero because the PATCH contract rejects null for low paths. Versions
     * are read immediately before restoring because they are managed by optimistic locking.
     */
    @After("@EnforcementDataRevert")
    public void restoreOriginalEnforcementAccountTypeValues() {
        if (originalAccountTypes.isEmpty()) {
            return;
        }

        try {
            Response currentResponse = getEnforcementAccountTypes(snapshotToken);
            currentResponse.then().statusCode(200);

            List<Map<String, Object>> restoreRequest = originalAccountTypes.entrySet().stream()
                .map(entry -> buildRestoreRequest(entry.getValue(), currentResponse))
                .toList();

            Response restoreResponse = patchEnforcementAccountTypes(snapshotToken, restoreRequest);
            restoreResponse.then().statusCode(200);

            Response restoredResponse = getEnforcementAccountTypes(snapshotToken);
            restoredResponse.then().statusCode(200);
            originalAccountTypes.forEach((accountType, original) -> assertMinimumBalance(
                accountType,
                minimumBalanceForRestore(original),
                restoredResponse
            ));
        } finally {
            originalAccountTypes.clear();
            submittedUpdates = List.of();
            updateResponse = null;
            snapshotToken = null;
        }
    }

    /**
     * Requests all enforcement account types as a specific test user.
     *
     * @param user user email used to resolve a bearer token.
     */
    @When("the {string} user requests all enforcement account types")
    public void userRequestsAllEnforcementAccountTypes(String user) {
        getEnforcementAccountTypes(BearerTokenStepDef.getAccessTokenForUser(user));
    }

    /**
     * Requests all enforcement account types with an invalid bearer token.
     */
    @When("all enforcement account types are requested with an invalid token")
    public void requestAllEnforcementAccountTypesWithInvalidToken() {
        getEnforcementAccountTypes("invalid-token");
    }

    /**
     * Captures the current mutable values needed to update and restore selected account types.
     *
     * @param accountTypes enforcement account type business codes to remember.
     */
    @Given("the original values are remembered for the following enforcement account types")
    public void rememberOriginalEnforcementAccountTypeValues(DataTable accountTypes) {
        snapshotToken = BearerTokenStepDef.getToken();
        Response response = getEnforcementAccountTypes(snapshotToken);
        response.then().statusCode(200);

        accountTypes.asMaps().forEach(row -> {
            String accountType = requiredValue(row, "enforcement_account_type");
            originalAccountTypes.put(accountType, snapshot(accountType, response));
        });
    }

    /**
     * Updates multiple enforcement account type minimum balances in one PATCH request.
     *
     * @param updates requested enforcement account type values.
     */
    @When("I update the following enforcement account type minimum balances")
    public void updateEnforcementAccountTypeMinimumBalances(DataTable updates) {
        submittedUpdates = updates.asMaps().stream()
            .map(this::toUpdate)
            .toList();

        List<Map<String, Object>> request = submittedUpdates.stream()
            .map(this::buildUpdateRequest)
            .toList();
        updateResponse = patchEnforcementAccountTypes(BearerTokenStepDef.getToken(), request);
    }

    /**
     * Attempts to clear the minimum balance for the selected enforcement account type.
     *
     * @param accountType enforcement account type business code to update.
     */
    @When("I update enforcement account type {string} to have a null minimum balance")
    public void clearEnforcementAccountTypeMinimumBalance(String accountType) {
        EnforcementAccountTypeSnapshot original = originalAccountTypes.get(accountType);
        assertNotNull(original, "Original values were not remembered for " + accountType);

        Map<String, Object> request = buildPatchRequest(original.id(), original.version(), null);
        updateResponse = patchEnforcementAccountTypes(BearerTokenStepDef.getToken(), List.of(request));
    }

    /**
     * Attempts an update using the version immediately before the current database version.
     *
     * @param accountType enforcement account type business code to update.
     */
    @When("I update enforcement account type {string} using an outdated version")
    public void updateEnforcementAccountTypeUsingOutdatedVersion(String accountType) {
        EnforcementAccountTypeSnapshot original = originalAccountTypes.get(accountType);
        assertNotNull(original, "Original values were not remembered for " + accountType);

        Map<String, Object> request = buildPatchRequest(
            original.id(),
            original.version() - 1,
            BigDecimal.valueOf(200)
        );
        updateResponse = patchEnforcementAccountTypes(BearerTokenStepDef.getToken(), List.of(request));
    }

    /**
     * Attempts an update using an identifier outside the seeded enforcement account type range.
     */
    @When("I update an enforcement account type that does not exist")
    public void updateNonExistentEnforcementAccountType() {
        Map<String, Object> request = buildPatchRequest(
            NON_EXISTENT_ENFORCEMENT_ACCOUNT_TYPE_ID,
            1L,
            BigDecimal.valueOf(200)
        );
        updateResponse = patchEnforcementAccountTypes(BearerTokenStepDef.getToken(), List.of(request));
    }

    /**
     * Asserts that a rejected PATCH did not change the selected enforcement account type.
     *
     * @param accountType enforcement account type business code expected to be unchanged.
     */
    @Then("enforcement account type {string} should remain unchanged")
    public void enforcementAccountTypeShouldRemainUnchanged(String accountType) {
        EnforcementAccountTypeSnapshot original = originalAccountTypes.get(accountType);
        assertNotNull(original, "Original values were not remembered for " + accountType);

        Response response = getEnforcementAccountTypes(snapshotToken);
        response.then().statusCode(200);
        Map<String, Object> actual = findAccountType(accountType, response);
        assertEquals(original.id(), ((Number) actual.get("id")).longValue());
        assertEquals(original.version(), ((Number) actual.get("version")).longValue());
        assertBigDecimalEquals(original.minimumBalance(), toBigDecimal(actual.get("minimum_balance")));
    }

    /**
     * Asserts both the PATCH response and a subsequent GET contain the updated values.
     *
     * @param expectedUpdates expected enforcement account type values.
     */
    @Then("the enforcement account type minimum balances should be updated with the following")
    public void updatedEnforcementAccountTypesShouldBeReturnedAndPersisted(DataTable expectedUpdates) {
        List<EnforcementAccountTypeUpdate> expected = expectedUpdates.asMaps().stream()
            .map(this::toUpdate)
            .toList();
        assertEquals(expected, submittedUpdates);

        List<Map<String, Object>> responseAccountTypes = updateResponse.jsonPath()
            .getList(ENFORCEMENT_ACCOUNT_TYPES);
        assertEquals(expected.size(), responseAccountTypes.size());
        expected.forEach(update -> assertUpdatedAccountType(update, updateResponse));

        Response persistedResponse = getEnforcementAccountTypes(snapshotToken);
        persistedResponse.then().statusCode(200);
        expected.forEach(update -> assertUpdatedAccountType(update, persistedResponse));
    }

    /**
     * Asserts the successful response matches the documented response shape and seeded values.
     */
    @Then("all expected enforcement account types should be returned")
    public void enforcementAccountTypesResponseIsReturnedAsDocumented() {
        then().statusCode(200);

        Response response = lastResponse();
        Map<String, Object> root = response.jsonPath().getMap("");
        assertEquals(TOP_LEVEL_FIELDS, root.keySet());

        List<Map<String, Object>> accountTypes = response.jsonPath().getList(ENFORCEMENT_ACCOUNT_TYPES);
        assertEquals(EXPECTED_ENFORCEMENT_ACCOUNT_TYPES.size(), accountTypes.size());
        assertEquals(
            EXPECTED_ENFORCEMENT_ACCOUNT_TYPES,
            accountTypes.stream()
                .map(accountType -> (String) accountType.get("enforcement_account_type"))
                .collect(Collectors.toSet())
        );

        accountTypes.forEach(this::assertEnforcementAccountType);
    }

    /**
     * Asserts the latest enforcement account types response is forbidden.
     */
    @Then("the enforcement account types request should be rejected as forbidden")
    public void enforcementAccountTypesRequestIsRejectedAsForbidden() {
        then()
            .statusCode(403)
            .body("title", equalTo("Forbidden"))
            .body("detail", equalTo("You do not have permission to access this resource"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/forbidden"))
            .body("retriable", equalTo(false))
            .body("instance", notNullValue())
            .body("operation_id", notNullValue());
    }

    /**
     * Asserts the latest enforcement account types response is unauthorized.
     */
    @Then("the enforcement account types request should be rejected as unauthorized")
    public void enforcementAccountTypesRequestIsRejectedAsUnauthorized() {
        then()
            .statusCode(401)
            .body("title", equalTo("Unauthorized"))
            .body("detail", equalTo("You are not authorized to access this resource"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/unauthorized"))
            .body("retriable", equalTo(false))
            .body("instance", notNullValue())
            .body("operation_id", notNullValue());
    }

    private Response getEnforcementAccountTypes(String token) {
        return given()
            .accept("*/*")
            .contentType("application/json")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .when()
            .get(getTestUrl() + ENFORCEMENT_ACCOUNT_TYPES_PATH);
    }

    private void assertEnforcementAccountType(Map<String, Object> accountType) {
        assertTrue(
            ENFORCEMENT_ACCOUNT_TYPE_FIELDS.containsAll(accountType.keySet()),
            "Response contains undocumented enforcement account type fields: " + accountType.keySet()
        );
        assertTrue(accountType.containsKey("id"), "id must be present");
        assertTrue(accountType.containsKey("version"), "version must be present");
        assertTrue(accountType.containsKey("account_type"), "account_type must be present");
        assertTrue(
            accountType.containsKey("enforcement_account_type"),
            "enforcement_account_type must be present"
        );
        assertTrue(accountType.containsKey("path"), "path must be present");
        assertTrue(((Number) accountType.get("id")).longValue() > 0, "id must be positive");
        assertTrue(((Number) accountType.get("version")).longValue() > 0, "version must be positive");
        assertTrue(Set.of("COL", "A", "CO", "Y").contains(accountType.get("account_type")));
        assertTrue(Set.of("H", "L").contains(accountType.get("path")));
    }

    private Response patchEnforcementAccountTypes(String token, List<Map<String, Object>> request) {
        return given()
            .accept("*/*")
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .body(request)
            .when()
            .patch(getTestUrl() + ENFORCEMENT_ACCOUNT_TYPES_PATH);
    }

    private EnforcementAccountTypeSnapshot snapshot(String accountType, Response response) {
        Map<String, Object> accountTypeData = findAccountType(accountType, response);
        return new EnforcementAccountTypeSnapshot(
            ((Number) accountTypeData.get("id")).longValue(),
            ((Number) accountTypeData.get("version")).longValue(),
            toBigDecimal(accountTypeData.get("minimum_balance")),
            (String) accountTypeData.get("path")
        );
    }

    private EnforcementAccountTypeUpdate toUpdate(Map<String, String> row) {
        return new EnforcementAccountTypeUpdate(
            requiredValue(row, "enforcement_account_type"),
            new BigDecimal(requiredValue(row, "minimum_balance"))
        );
    }

    private Map<String, Object> buildUpdateRequest(EnforcementAccountTypeUpdate update) {
        EnforcementAccountTypeSnapshot original = originalAccountTypes.get(update.accountType());
        assertNotNull(original, "Original values were not remembered for " + update.accountType());
        return buildPatchRequest(original.id(), original.version(), update.minimumBalance());
    }

    private Map<String, Object> buildRestoreRequest(EnforcementAccountTypeSnapshot original, Response response) {
        Map<String, Object> current = findAccountTypeById(original.id(), response);
        return buildPatchRequest(
            original.id(),
            ((Number) current.get("version")).longValue(),
            minimumBalanceForRestore(original)
        );
    }

    private BigDecimal minimumBalanceForRestore(EnforcementAccountTypeSnapshot original) {
        return original.minimumBalance() == null && "L".equals(original.path())
            ? BigDecimal.ZERO
            : original.minimumBalance();
    }

    private Map<String, Object> buildPatchRequest(long id, long version, BigDecimal minimumBalance) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("id", id);
        request.put("version", version);
        request.put("minimum_balance", minimumBalance);
        return request;
    }

    private void assertUpdatedAccountType(EnforcementAccountTypeUpdate update, Response response) {
        EnforcementAccountTypeSnapshot original = originalAccountTypes.get(update.accountType());
        Map<String, Object> actual = findAccountType(update.accountType(), response);
        assertEquals(original.id(), ((Number) actual.get("id")).longValue());
        assertEquals(original.version() + 1, ((Number) actual.get("version")).longValue());
        assertBigDecimalEquals(update.minimumBalance(), toBigDecimal(actual.get("minimum_balance")));
    }

    private void assertMinimumBalance(String accountType, BigDecimal expected, Response response) {
        BigDecimal actual = toBigDecimal(findAccountType(accountType, response).get("minimum_balance"));
        assertBigDecimalEquals(expected, actual);
    }

    private Map<String, Object> findAccountType(String accountType, Response response) {
        return accountTypes(response).stream()
            .filter(candidate -> accountType.equals(candidate.get("enforcement_account_type")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Enforcement account type not found: " + accountType));
    }

    private Map<String, Object> findAccountTypeById(long id, Response response) {
        return accountTypes(response).stream()
            .filter(candidate -> ((Number) candidate.get("id")).longValue() == id)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Enforcement account type id not found: " + id));
    }

    private List<Map<String, Object>> accountTypes(Response response) {
        return response.jsonPath().getList(ENFORCEMENT_ACCOUNT_TYPES);
    }

    private String requiredValue(Map<String, String> row, String field) {
        String value = row.get(field);
        assertNotNull(value, "Missing DataTable field: " + field);
        return value;
    }

    private BigDecimal toBigDecimal(Object value) {
        return value == null ? null : new BigDecimal(value.toString());
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        if (expected == null || actual == null) {
            assertEquals(expected, actual);
            return;
        }
        assertEquals(0, expected.compareTo(actual));
    }

    private record EnforcementAccountTypeSnapshot(long id, long version, BigDecimal minimumBalance, String path) {
    }

    private record EnforcementAccountTypeUpdate(String accountType, BigDecimal minimumBalance) {
    }
}
