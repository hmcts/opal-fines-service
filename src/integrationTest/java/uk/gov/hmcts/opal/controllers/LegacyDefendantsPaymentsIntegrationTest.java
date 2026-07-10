package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.LegacyDefendantsUtil.getPaymentTermsRequestSampleAsJson;
import static uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountPaymentTermsService.ADD_PAYMENT_CARD_REQUEST;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyResponse;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsPaymentsIntegrationTest")
class LegacyDefendantsPaymentsIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    @MockitoSpyBean
    private GatewayService gatewayService;

    @Test
    @DisplayName("LEGACY: Add Payment Card Request – Happy Path [@PO-2088]")
    @JiraStory("PO-2088")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-5941")
    void testAddPaymentCardRequest_Happy() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "3");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/901/payment-card-request")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraStory("PO-2088")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-5938")
    void testAddPaymentCardRequest_500() throws Exception {
        doThrow(HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            HttpHeaders.EMPTY,
            null,
            null
        )).when(gatewayService).postToGateway(
            eq(ADD_PAYMENT_CARD_REQUEST),
            eq(AddPaymentCardLegacyResponse.class),
            any(AddPaymentCardLegacyRequest.class),
            isNull()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "1");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/555/payment-card-request")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
        );

        log.info(":legacyAddPaymentCardRequest_500 body:\n{}", result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("LEGACY: POST Add Payment Terms - Success")
    @JiraStory("PO-2087")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-5939")
    void addPaymentTerms_whenGatewayResponseWithSuccess_thenReturnMappedResponse() throws Exception {
        userStateStub.addPermissions((short) 69, FinesPermission.values());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "69");
        headers.add(HttpHeaders.IF_MATCH, "\"1\"");
        var response = mockMvc.perform(
            post("/defendant-accounts/69/payment-terms")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @DisplayName("LEGACY: POST Add Payment Terms - Handle 500 error from the gateway")
    @JiraStory("PO-2087")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-5940")
    void addPaymentTerms_whenGatewayResponseWithException_thenDoNotReturnEntity() throws Exception {
        userStateStub.addPermissions((short) 500, FinesPermission.values());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "500");
        headers.add(HttpHeaders.IF_MATCH, "\"1\"");

        var response = mockMvc.perform(
            post("/defendant-accounts/500/payment-terms")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
