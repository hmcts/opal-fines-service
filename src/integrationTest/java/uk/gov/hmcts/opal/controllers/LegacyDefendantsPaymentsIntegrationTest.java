package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.LegacyDefendantsUtil.getPaymentTermsRequestSampleAsJson;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsPaymentsIntegrationTest")
class LegacyDefendantsPaymentsIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    @Test
    @DisplayName("LEGACY: Add Payment Card Request – Happy Path [@PO-2088]")
    void testAddPaymentCardRequest_Happy() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "3");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/901/payment-card-request")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":legacyAddPaymentCardRequest_Happy body:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(901));
    }

    @Test
    @DisplayName("LEGACY: Add Payment Card Request – 500 Error [@PO-2088]")
    void testAddPaymentCardRequest_500() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "1");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/555/payment-card-request")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
        );

        log.info(":legacyAddPaymentCardRequest_500 body:\n{}", result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().is5xxServerError());
    }

    @Test
    @Disabled("Fails against current legacy stub mappings")
    @DisplayName("LEGACY: POST Add Payment Terms - Success")
    void addPaymentTerms_whenGatewayResponseWithSuccess_thenReturnMappedResponse() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "69");
        headers.add(HttpHeaders.IF_MATCH, "\"1\"");

        var response = mockMvc.perform(
            post("/defendant-accounts/69/payment-terms")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getPaymentTermsRequestSampleAsJson())
        );

        response.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payment_terms").exists())
            .andExpect(jsonPath("$.payment_terms.days_in_default").value(14))
            .andExpect(jsonPath("$.payment_card_last_requested").value("2026-02-20"))
            .andExpect(jsonPath("$.last_enforcement").value("NOTICE_SENT"));
    }

    @Test
    @Disabled("Fails against current legacy stub mappings")
    @DisplayName("LEGACY: POST Add Payment Terms - Handle 500 error from the gateway")
    void addPaymentTerms_whenGatewayResponseWithException_thenDoNotReturnEntity() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "500");
        headers.add(HttpHeaders.IF_MATCH, "\"1\"");

        var response = mockMvc.perform(
            post("/defendant-accounts/500/payment-terms")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getPaymentTermsRequestSampleAsJson())
        );

        response.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.payment_terms").doesNotExist())
            .andExpect(jsonPath("$.payment_terms.days_in_default").doesNotExist())
            .andExpect(jsonPath("$.payment_card_last_requested").doesNotExist())
            .andExpect(jsonPath("$.last_enforcement").doesNotExist());
    }
}
