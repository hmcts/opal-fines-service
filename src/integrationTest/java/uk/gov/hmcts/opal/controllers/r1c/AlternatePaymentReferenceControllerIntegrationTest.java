package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES;
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertGet200JsonResponseMatchesBundledSpec;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectEntityNotFound;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectError;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-banking-interfaces=true")
@Sql(scripts = "classpath:db/insertData/insert_into_alternate_payment_references.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_alternate_payment_references.sql",
    executionPhase = AFTER_TEST_METHOD)
@DisplayName("Alternate payment reference controller integration tests")
class AlternatePaymentReferenceControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ENDPOINT = "/defendant-accounts/{defendantAccountId}/alternate-payment-references";
    private static final short BUSINESS_UNIT_ID = 10877;
    private static final long ACCOUNT_WITH_REFERENCES = 1087701L;
    private static final long ACCOUNT_WITHOUT_REFERENCES = 1087702L;

    @BeforeEach
    void setUpPermissions() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, MANAGE_ALTERNATE_PAYMENT_REFERENCES);
    }

    @Nested
    @DisplayName("Get alternate payment references for a defendant account")
    class GetAlternatePaymentReferences {

        @Test
        @JiraStory("PO-10877")
        @DisplayName("Returns multiple APRs with all mapped fields and excludes other accounts")
        void returnsMultipleReferencesForRequestedAccountOnly() throws Exception {
            ResultActions result = getReferences(ACCOUNT_WITH_REFERENCES)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(content().json("""
                    [
                      {
                        "alternate_payment_reference_id": 10877001,
                        "defendant_account_id": 1087701,
                        "relationship": "APR",
                        "category": "APR",
                        "business_unit_id": 10877,
                        "apr_text": "APR-REFERENCE-1",
                        "created_at": "2026-10-20T09:15:30",
                        "updated_at": "2026-10-21T10:20:40",
                        "version": 2
                      },
                      {
                        "alternate_payment_reference_id": 10877002,
                        "defendant_account_id": 1087701,
                        "relationship": "CONSOLIDATED",
                        "category": "ACC",
                        "business_unit_id": 10877,
                        "apr_text": "APR-REFERENCE-2",
                        "created_at": "2026-10-18T11:25:35",
                        "updated_at": "2026-10-19T12:30:45",
                        "version": 4
                      }
                    ]
                    """));

            assertGet200JsonResponseMatchesBundledSpec(
                objectMapper.readTree(result.andReturn().getResponse().getContentAsString()), ENDPOINT);
        }

        @Test
        @JiraStory("PO-10877")
        @DisplayName("Returns an empty array when the defendant account has no APRs")
        void returnsEmptyArrayWhenNoReferencesExist() throws Exception {
            getReferences(ACCOUNT_WITHOUT_REFERENCES)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @JiraStory("PO-10877")
        @DisplayName("Returns not found when the defendant account does not exist")
        void returnsNotFoundWhenDefendantAccountDoesNotExist() throws Exception {
            getReferences(1087799L)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(expectEntityNotFound());
        }

        @Test
        @JiraStory("PO-10877")
        @DisplayName("Returns forbidden when the authenticated user has no permissions")
        void returnsForbiddenWhenUserHasNoPermissions() throws Exception {
            userStateStub.setupWithNoPermissions();

            assertForbidden(getReferences(ACCOUNT_WITH_REFERENCES));
        }

        @Test
        @JiraStory("PO-10877")
        @DisplayName("Returns forbidden when the user has APR permission only in another business unit")
        void returnsForbiddenWhenPermissionIsForAnotherBusinessUnit() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 78, MANAGE_ALTERNATE_PAYMENT_REFERENCES);

            assertForbidden(getReferences(ACCOUNT_WITH_REFERENCES));
        }
    }

    private ResultActions getReferences(long defendantAccountId) throws Exception {
        return mockMvc.perform(get(ENDPOINT, defendantAccountId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .accept(MediaType.APPLICATION_JSON));
    }

    private void assertForbidden(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(expectError(HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to access this resource", "https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
