package uk.gov.hmcts.opal.assertions.draftaccount;

import io.restassured.response.Response;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Provides reusable assertions for draft-account API responses.
 */
public class DraftAccountAssertions {
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Asserts the full happy-path create response, including status, headers, and expected body
     * fields.
     *
     * @param response create response to inspect.
     * @param expectedData field names and values expected in the response body.
     */
    public void assertCreatedDraftAccount(Response response, Map<String, String> expectedData) {
        responseAssertions.assertStatus(response, 201);
        responseAssertions.assertStrongQuotedEtag(response);
        responseAssertions.assertBodyDoesNotContainField(response, "version");
        responseAssertions.assertResponseContains(response, expectedData);
    }

    /**
     * Asserts the full happy-path retrieval response, including status, headers, and expected body
     * fields.
     *
     * @param response retrieval response to inspect.
     * @param expectedData field names and values expected in the response body.
     */
    public void assertRetrievedDraftAccount(Response response, Map<String, String> expectedData) {
        responseAssertions.assertStatus(response, 200);
        responseAssertions.assertStrongQuotedEtag(response);
        responseAssertions.assertBodyDoesNotContainField(response, "version");
        responseAssertions.assertResponseContains(response, expectedData);
    }

    /**
     * Asserts that the replacement flow preserved the original `created_at` timestamp.
     *
     * @param response latest draft-account response after replacement.
     * @param expectedCreatedAt original `created_at` value captured before replacement.
     */
    public void assertCreatedAtUnchanged(Response response, String expectedCreatedAt) {
        Instant apiCreatedAt = Instant.parse(response.jsonPath().getString("created_at"));
        Instant originalCreatedAt = Instant.parse(expectedCreatedAt);

        String actual = String.valueOf(apiCreatedAt.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        String expected = String.valueOf(originalCreatedAt.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));

        Serenity.recordReportData().withTitle("Times").andContents(
            "Created at time: " + expected + "\nResponse created at time: " + actual);

        assertEquals(expected, actual, "Created at time has changed");
    }

    /**
     * Asserts that the replacement flow moved `account_status_date` forward.
     *
     * @param response latest draft-account response after replacement.
     * @param initialAccountStatusDate original `account_status_date` captured before replacement.
     */
    public void assertAccountStatusDateAfter(Response response, String initialAccountStatusDate) {
        Instant apiAccountStatusDate = Instant.parse(response.jsonPath().getString("account_status_date"));
        Instant originalAccountStatusDate = Instant.parse(initialAccountStatusDate);

        String original = String.valueOf(originalAccountStatusDate.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        String current = String.valueOf(apiAccountStatusDate.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));

        Serenity.recordReportData().withTitle("Times").andContents(
            "Initial account status date: " + original + "\nResponse account status date: " + current);

        assertTrue(
            apiAccountStatusDate.isAfter(originalAccountStatusDate),
            "Account status date is not after the initial account status date"
        );
    }

    /**
     * Asserts that a draft-account summary response returned HTTP 200 and that every returned
     * summary matches the supplied expected values.
     *
     * @param response latest draft-account summary response to inspect.
     * @param expectedData field names and values expected in each returned summary item.
     * @param caseSensitive whether value comparisons should be case sensitive.
     */
    public void assertSummaryFields(Response response, Map<String, String> expectedData, boolean caseSensitive) {
        responseAssertions.assertStatus(response, 200);

        int count = response.jsonPath().getInt("count");
        for (Map.Entry<String, String> entry : expectedData.entrySet()) {
            for (int i = 0; i < count; i++) {
                String actual = response.jsonPath().getString("summaries[" + i + "]." + entry.getKey());
                if (caseSensitive) {
                    assertEquals(entry.getValue(), actual, "Values are not equal : ");
                } else {
                    assertTrue(
                        String.valueOf(entry.getValue()).equalsIgnoreCase(String.valueOf(actual)),
                        "Value " + actual + " is not equal to expected (case-insensitive): '" + entry.getValue() + "'"
                    );
                }
            }
        }
    }

    /**
     * Asserts that the latest draft-account summary response returned HTTP 200 and that every
     * returned summary matches the supplied expected values.
     *
     * @param expectedData field names and values expected in each returned summary item.
     * @param caseSensitive whether value comparisons should be case sensitive.
     */
    public void assertLatestSummaryFields(Map<String, String> expectedData, boolean caseSensitive) {
        assertSummaryFields(SerenityRest.lastResponse(), expectedData, caseSensitive);
    }

    /**
     * Asserts that the latest summary response does not contain the supplied value in the named
     * summary field.
     *
     * @param fieldName summary field name to inspect.
     * @param unexpectedValue value that must not appear in the response field.
     */
    public void assertLatestSummaryFieldDoesNotContain(String fieldName, String unexpectedValue) {
        assertLatestSummaryFieldDoesNotContainAny(fieldName, java.util.List.of(unexpectedValue));
    }

    /**
     * Asserts that the latest summary response does not contain any of the supplied values in the
     * named summary field.
     *
     * @param fieldName summary field name to inspect.
     * @param unexpectedValues values that must not appear in the response field.
     */
    public void assertLatestSummaryFieldDoesNotContainAny(String fieldName, Collection<String> unexpectedValues) {
        Response response = SerenityRest.lastResponse();
        int count = response.jsonPath().getInt("count");

        for (int i = 0; i < count; i++) {
            String actual = response.jsonPath().getString("summaries[" + i + "]." + fieldName);
            for (String unexpectedValue : unexpectedValues) {
                assertNotEquals(unexpectedValue, actual, "should not contain " + unexpectedValue);
            }
        }
    }
}
