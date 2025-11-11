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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;

@DisplayName("Integration tests for /defendant-accounts/{id}/header-summary")
@Slf4j
public class DefendantAccountHeaderSummaryIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    protected final String getHeaderSummaryResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountHeaderSummaryResponse.json";
    }


    @Nested
    @DisplayName("Legacy Tests")
    @ActiveProfiles({"integration", "legacy"})
    @AutoConfigureMockMvc
    public class Legacy extends Common {

        @DisplayName("Get header summary for non-existent ID returns 500")
        @Test
        public void getHeaderSummary_Legacy_500() throws Exception {
            ResultActions ra = mockMvc.perform(
                get(URL_BASE + "/500/header-summary")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = ra.andReturn().getResponse().getContentAsString();
            log.info(":getHeaderSummary_Legacy_500: Response body:\n{}", ToJsonString.toPrettyJson(body));

            ra.andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
        }

    }

    @Nested
    @DisplayName("Opal Tests")
    @ActiveProfiles({"integration", "opal"})
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
    public class Opal extends Common {

        @DisplayName("Get header summary for non-existent ID returns 404")
        @Test
        public void getHeaderSummary_Opal_NotFound() throws Exception {
            ResultActions ra = mockMvc.perform(
                get(URL_BASE + "/500/header-summary")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = ra.andReturn().getResponse().getContentAsString();
            log.info(":getHeaderSummary_Opal_NotFound: Response body:\n{}", ToJsonString.toPrettyJson(body));

            ra.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
                .andExpect(jsonPath("$.status").value(404));
        }

        @DisplayName("PO-2297: header-summary (individual) returns correct defendant_party_id from "
            + "defendantAccountPartyId bug fix validation")
        @Test
        public void testGetHeaderSummary_Individual_UsesDefendantAccountPartyId() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                get("/defendant-accounts/77/header-summary")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info("PO-2297 Individual header summary response:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party_id").value("77"))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
                .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
                .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"));

            validateJsonSchema(body, getHeaderSummaryResponseSchemaLocation());
        }

        @DisplayName("PO-2297: header-summary (organisation) returns correct defendant_party_id from"
            + " defendantAccountPartyId — bug fix validation")
        @Test
        public void testGetHeaderSummary_Organisation_UsesDefendantAccountPartyId() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                get("/defendant-accounts/10001/header-summary")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info("PO-2297 Organisation header summary response:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party_id").value("10001"))
                .andExpect(jsonPath("$.party_details.party_id").value("10001"))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"));

            validateJsonSchema(body, getHeaderSummaryResponseSchemaLocation());
        }

        @Test
        void testGetHeaderSummary_ThrowsNotFound() throws Exception {
            ResultActions resultActions = mockMvc.perform(get("/defendant-accounts/999777/header-summary")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetHeaderSummary_ThrowsNotFound: Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isNotFound());
        }

        @DisplayName("PO-2119 / Problem JSON contains retriable field")
        @Test
        public void testEntityNotFoundExceptionContainsRetriable() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/12345/header-summary")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testRetriableIncludedInProblemDetail: Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
                .andExpect(jsonPath("$.retriable").value(false));
        }
    }


    public abstract class Common extends AbstractIntegrationTest {

        @DisplayName("Get header summary for individual defendant account [@PO-2287]")
        @Test
        public void getHeaderSummary_Individual() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/header-summary")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetHeaderSummary_Individual: Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_id").value("77"))
                .andExpect(jsonPath("$.account_number").value("177A"))
                .andExpect(jsonPath("$.debtor_type").value("Defendant"))
                .andExpect(jsonPath("$.is_youth").value(false))
                .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("888"))
                .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value("78"))
                .andExpect(jsonPath("$.payment_state_summary.imposed_amount").value(700.58))
                .andExpect(jsonPath("$.payment_state_summary.paid_amount").value(200.00))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
                .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
                .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
                .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

            validateJsonSchema(body, getHeaderSummaryResponseSchemaLocation());
        }

        @DisplayName("Get header summary for organisation defendant account [@PO-2287]")
        @Test
        public void getHeaderSummary_Organisation() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/header-summary")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetHeaderSummary_Organisation: Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_id").value("10001"))
                .andExpect(jsonPath("$.account_number").value("10001A"))
                .andExpect(jsonPath("$.debtor_type").value("Defendant"))
                .andExpect(jsonPath("$.is_youth").value(false))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
                .andExpect(jsonPath("$.party_details.individual_details").doesNotExist());

            validateJsonSchema(body, getHeaderSummaryResponseSchemaLocation());
        }
    }
}
