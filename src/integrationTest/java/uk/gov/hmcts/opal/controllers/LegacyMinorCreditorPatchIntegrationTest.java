package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.LegacyMinorCreditorPatchIntegrationTest")
class LegacyMinorCreditorPatchIntegrationTest extends MinorCreditorControllerIntegrationTest {

    private static final String AUTH_HEADER = "Bearer some_value";
    private static final String URL_BASE = "/minor-creditor-accounts";
    private static final long PATCH_MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID = 10;
    private static final String UPDATE_MINOR_CREDITOR_ACCOUNT = "LIBRA.of_update_minor_creditor_account";

    @MockitoBean
    private GatewayService gatewayService;

    @Test
    void patchMinorCreditor_success_returns200() throws Exception {
        authorisePatchUser();
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyPatchResponse()));

        ResultActions resultActions = performLegacyPatch(PATCH_MINOR_CREDITOR_ACCOUNT_ID, "\"1\"");

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditor_success_returns200 body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.creditor_account_id").value(PATCH_MINOR_CREDITOR_ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.party_id").value("99008"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Updated Ltd"))
            .andExpect(jsonPath("$.address.address_line_1").value("99 Updated Road"))
            .andExpect(jsonPath("$.payment.account_name").value("Updated Account"))
            .andExpect(jsonPath("$.payment.sort_code").value("112233"))
            .andExpect(jsonPath("$.payment.account_number").value("12345678"))
            .andExpect(jsonPath("$.payment.account_reference").value("Ref-01"))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.payment.hold_payment").value(true));

        ArgumentCaptor<LegacyUpdateMinorCreditorAccountRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyUpdateMinorCreditorAccountRequest.class);

        verify(gatewayService).postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            requestCaptor.capture(),
            eq(null)
        );

        LegacyUpdateMinorCreditorAccountRequest legacyRequest = requestCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("607", legacyRequest.getCreditorAccountId());
        org.junit.jupiter.api.Assertions.assertEquals("10", legacyRequest.getBusinessUnitId());
        org.junit.jupiter.api.Assertions.assertEquals("USER01", legacyRequest.getBusinessUnitUserId());
        org.junit.jupiter.api.Assertions.assertEquals(1, legacyRequest.getAccountVersion());
        org.junit.jupiter.api.Assertions.assertEquals("99008", legacyRequest.getPartyDetails().getPartyId());
        org.junit.jupiter.api.Assertions.assertEquals("Updated Ltd",
            legacyRequest.getPartyDetails().getOrganisationDetails().getOrganisationName());
        org.junit.jupiter.api.Assertions.assertEquals("99 Updated Road", legacyRequest.getAddress().getAddressLine1());
        org.junit.jupiter.api.Assertions.assertEquals("112233", legacyRequest.getPayment().getSortCode());
        org.junit.jupiter.api.Assertions.assertEquals("12345678", legacyRequest.getPayment().getAccountNumber());
        org.junit.jupiter.api.Assertions.assertEquals("Ref-01", legacyRequest.getPayment().getAccountReference());
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, legacyRequest.getPayment().getPayByBacs());
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, legacyRequest.getPayment().getHoldPayment());
    }

    @Test
    void patchMinorCreditor_notFound_returns404() throws Exception {
        authorisePatchUser();
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        performLegacyPatch(404L, "\"1\"")
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_timeout_returns408() throws Exception {
        authorisePatchUser();
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpClientErrorException.create(
            HttpStatusCode.valueOf(408), "Request Timeout", HttpHeaders.EMPTY, null, null));

        performLegacyPatch(408L, "\"1\"")
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_staleVersion_returns409() throws Exception {
        authorisePatchUser();
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpClientErrorException.create(
            HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, null, null));

        performLegacyPatch(409L, "\"2\"")
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_serviceUnavailable_returns503() throws Exception {
        authorisePatchUser();
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpServerErrorException.create(
            HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", HttpHeaders.EMPTY, null, null));

        performLegacyPatch(503L, "\"1\"")
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_serverError_returns500() throws Exception {
        authorisePatchUser();
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, null, null));

        performLegacyPatch(500L, "\"1\"")
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    private void authorisePatchUser() {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));
    }

    private ResultActions performLegacyPatch(long creditorAccountId, String ifMatch) throws Exception {
        return mockMvc.perform(
            patch(URL_BASE + "/" + creditorAccountId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", ifMatch)
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
        );
    }

    private LegacyUpdateMinorCreditorAccountResponse legacyPatchResponse() {
        return LegacyUpdateMinorCreditorAccountResponse.builder()
            .accountVersion(2)
            .creditorAccountId(PATCH_MINOR_CREDITOR_ACCOUNT_ID)
            .partyDetails(LegacyPartyDetails.builder()
                .partyId("99008")
                .organisationFlag(true)
                .organisationDetails(OrganisationDetails.builder().organisationName("Updated Ltd").build())
                .build())
            .address(AddressDetailsLegacy.builder()
                .addressLine1("99 Updated Road")
                .addressLine2("Updated Area")
                .addressLine3("Updated Town")
                .postcode("NW1 1AA")
                .build())
            .payment(LegacyCreditorAccountPaymentDetails.builder()
                .accountName("Updated Account")
                .sortCode("112233")
                .accountNumber("12345678")
                .accountReference("Ref-01")
                .payByBacs(true)
                .holdPayment(true)
                .build())
            .build();
    }

    private PatchMinorCreditorAccountRequest patchMinorCreditorLegacyRequest() {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                .partyId("99008")
                .organisationFlag(true)
                .organisationDetails(new OrganisationDetailsCommon().organisationName("Updated Ltd")))
            .address(new AddressDetailsCommon()
                .addressLine1("99 Updated Road")
                .addressLine2("Updated Area")
                .addressLine3("Updated Town")
                .postcode("NW1 1AA"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                .accountName("Updated Account")
                .sortCode("112233")
                .accountNumber("12345678")
                .accountReference("Ref-01")
                .payByBacs(true)
                .holdPayment(true));
    }
}
