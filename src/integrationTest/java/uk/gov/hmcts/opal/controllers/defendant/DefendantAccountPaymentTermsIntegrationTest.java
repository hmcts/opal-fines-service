package uk.gov.hmcts.opal.controllers.defendant;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;

@DisplayName("Integration tests for /defendant-accounts/{id}/payment-terms/latest")
@Slf4j
public class DefendantAccountPaymentTermsIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    protected final String getPaymentTermsResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPaymentTermsResponse.json";
    }

    // =====================================
    // LEGACY TESTS
    // =====================================
    @Nested
    @DisplayName("Legacy Tests")
    @ActiveProfiles({"integration", "legacy"})
    @AutoConfigureMockMvc
    public class Legacy extends AbstractIntegrationTest {

        @DisplayName("Get Defendant Account Payment Terms [@PO-1565] - Happy Path (legacy schema)")
        @Test
        public void testLegacyGetPaymentTerms() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                get(URL_BASE + "/77/payment-terms/latest")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testLegacyGetPaymentTerms: Response body:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payment_terms.days_in_default").value(120))
                .andExpect(jsonPath("$.payment_terms.date_days_in_default_imposed").value("2025-10-12"))
                .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
                .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
                .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
                .andExpect(jsonPath("$.payment_terms.posted_details.posted_date").value("2023-11-03"))
                .andExpect(jsonPath("$.payment_terms.posted_details.posted_by").value("01000000A"))
                .andExpect(jsonPath("$.payment_terms.posted_details.posted_by_name").value(""))
                .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
                .andExpect(jsonPath("$.payment_terms.extension").value(false))
                .andExpect(jsonPath("$.last_enforcement").value("REM"));

            validateJsonSchema(body, getPaymentTermsResponseSchemaLocation());
        }

        @DisplayName("Get Defendant Account Payment Terms [@PO-1565] - Internal Server Error (500)")
        @Test
        public void getDefendantAccountPaymentTerms_500Error() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                get(URL_BASE + "/500/payment-terms/latest")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":getDefendantAccountPaymentTerms_500Error response body:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
        }
    }

    // =====================================
    // OPAL TESTS
    // =====================================
    @Nested
    @DisplayName("Opal Tests")
    @ActiveProfiles({"integration", "opal"})
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
    public class Opal extends AbstractIntegrationTest {

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @DisplayName("OPAL: Get Defendant Account Payment Terms [@PO-1565] - Happy Path")
        @Test
        public void testGetPaymentTerms() throws Exception {
            // Make the 'date_last_amended' deterministic for acct 77
            jdbcTemplate.update(
                "UPDATE defendant_accounts SET last_changed_date = "
                    + "'2024-01-03 00:00:00' WHERE defendant_account_id = 77"
            );

            ResultActions resultActions = mockMvc.perform(
                get(URL_BASE + "/77/payment-terms/latest")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetPaymentTerms: Response body:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payment_terms.days_in_default").value(120))
                .andExpect(jsonPath("$.payment_terms.date_days_in_default_imposed").isEmpty())
                .andExpect(jsonPath("$.payment_terms.reason_for_extension").isEmpty())
                .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
                .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
                .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
                .andExpect(jsonPath("$.payment_terms.posted_details.posted_date").value("2023-11-03"))
                .andExpect(jsonPath("$.payment_terms.posted_details.posted_by").value("01000000A"))
                .andExpect(jsonPath("$.payment_terms.posted_details.posted_by_name").isEmpty())
                .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
                .andExpect(jsonPath("$.payment_terms.extension").value(false))
                .andExpect(jsonPath("$.last_enforcement").value("10"));

            validateJsonSchema(body, getPaymentTermsResponseSchemaLocation());
        }

        @DisplayName("OPAL: Get Defendant Account Payment Terms [@PO-1565] - Not Found (404)")
        @Test
        public void testGetPaymentTermsLatest_NoPaymentTermFoundForId() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                get(URL_BASE + "/79/payment-terms/latest")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetPaymentTermsLatest_NoPaymentTermFoundForId body:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));
        }
    }
}
