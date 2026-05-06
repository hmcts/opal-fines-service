package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
public class LegacyMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    private static final String URL_BASE = "/minor-creditor-accounts";
    private static final String AUTH_HEADER = "Bearer some_value";
    private static final long PATCH_MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID = 10;
    private static final String UPDATE_MINOR_CREDITOR_ACCOUNT = "LIBRA.of_update_minor_creditor_account";

    @MockitoBean
    private GatewayService gatewayService;

    @org.junit.jupiter.api.BeforeEach
    void setUpGatewayDefaults() {
        doAnswer(this::defaultGatewayResponse).when(gatewayService).postToGateway(any(), any(), any(), any());
    }

    @Test
    @JiraStory("PO-1902")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-5953")
    void testPostSearchMinorCreditorSuccess() throws Exception {
        super.postSearchMinorCreditorImpl_Success(log);
    }

    @Test
    @JiraStory("PO-1902")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-5949")
    void testPostSearchMinorCreditor_500Error() throws Exception {
        super.legacyPostSearchMinorCreditorImpl_500Error(log);
    }

    @Test
    @JiraStory("PO-1913")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5956")
    void testGetMinorCreditorAtAGlanceSuccess() throws Exception {
        super.getMinorCreditorAtAGlanceImpl_Success(log);
    }

    @Test
    @JiraStory("PO-1913")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5955")
    void testGetMinorCreditorAtAGlance_500Error() throws Exception {
        super.legacyGetMinorCreditorAtAGlanceImpl_500Error(log);
    }

    @Test
    @JiraStory("PO-1912")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5952")
    void testGetMinorCreditorHeaderSummarySuccess() throws Exception {
        super.getHeaderSummaryImpl_Success(log);
    }

    @Test
    @JiraStory("PO-1912")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5946")
    void testGetMinorCreditorHeaderSummary_500Error() throws Exception {
        super.legacyGetMinorCreditorHeaderSummaryImpl_500Error(log);
    }

    @Test
    @JiraStory("PO-1991")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5948")
    void testGetMinorCreditorAccountSuccess() throws Exception {
        super.getMinorCreditorAccountImpl_Success(log);
    }

    @Test
    @JiraStory("PO-1991")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5950")
    void testGetMinorCreditorAccountFiltersBacsWithoutPermission() throws Exception {
        super.getMinorCreditorAccountImpl_filtersBacsDetailsWithoutPermission(log);
    }

    @Test
    @JiraStory("PO-1991")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5954")
    void testGetMinorCreditorAccountMissingAuthHeaderReturns401() throws Exception {
        super.getMinorCreditorAccount_missingAuthHeader_returns401();
    }

    @Test
    @JiraStory("PO-1991")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5951")
    void testGetMinorCreditorAccountAuthenticatedWithoutPermissionReturns403() throws Exception {
        super.getMinorCreditorAccount_authenticatedWithoutPermission_returns403();
    }

    @Test
    @JiraStory("PO-1991")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5947")
    void testGetMinorCreditorAccount_500Error() throws Exception {
        super.legacyGetMinorCreditorAccountImpl_500Error(log);
    }

    @Test
    void patchMinorCreditor_success_returns201_andTransformsLegacyRequest() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.OK,
            legacyPatchResponse(),
            null,
            null
        ));

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", "\"1\"")
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditor_success_returns201_andTransformsLegacyRequest body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated())
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

        ArgumentCaptor<Object> requestCaptor = ArgumentCaptor.forClass(Object.class);
        verify(gatewayService).postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            requestCaptor.capture(),
            eq(null)
        );

        uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest legacyRequest =
            (uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest) requestCaptor.getValue();

        org.junit.jupiter.api.Assertions.assertEquals("607", legacyRequest.getCreditorAccountId());
        org.junit.jupiter.api.Assertions.assertEquals("10", legacyRequest.getBusinessUnitId());
        org.junit.jupiter.api.Assertions.assertEquals("USER01", legacyRequest.getBusinessUnitUserId());
        org.junit.jupiter.api.Assertions.assertEquals(1, legacyRequest.getAccountVersion());
        org.junit.jupiter.api.Assertions.assertEquals("Updated Ltd",
            legacyRequest.getPartyDetails().getOrganisationDetails().getOrganisationName());
        org.junit.jupiter.api.Assertions.assertEquals("112233", legacyRequest.getPayment().getSortCode());
        org.junit.jupiter.api.Assertions.assertEquals("Ref-01", legacyRequest.getPayment().getAccountReference());
    }

    @Test
    void patchMinorCreditor_withoutPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutPermission_returns403();
    }

    @Test
    void patchMinorCreditor_notFound_returns404() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        mockMvc.perform(
                patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", AUTH_HEADER)
                    .header("If-Match", "\"1\"")
                    .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                    .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_staleVersion_returns409() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null));

        mockMvc.perform(
                patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", AUTH_HEADER)
                    .header("If-Match", "\"2\"")
                    .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                    .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_withoutHoldPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutHoldPermission_returns403();
    }

    @Test
    void patchMinorCreditor_withoutAccountMaintenancePermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutAccountMaintenancePermission_returns403();
    }

    @Test
    void patchMinorCreditor_missingAuthHeader_returns401() throws Exception {
        super.patchMinorCreditor_missingAuthHeader_returns401();
    }

    @Test
    void patchMinorCreditor_timeout_returns408() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Timeout"));

        mockMvc.perform(
                patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", AUTH_HEADER)
                    .header("If-Match", "\"1\"")
                    .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                    .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
            )
            .andExpect(status().isRequestTimeout());
    }

    @Test
    void patchMinorCreditor_serviceUnavailable_returns503() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Gateway down", null, null,
            null));

        mockMvc.perform(
                patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", AUTH_HEADER)
                    .header("If-Match", "\"1\"")
                    .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                    .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
            )
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_serverError_returns500() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));
        when(gatewayService.postToGateway(
            eq(UPDATE_MINOR_CREDITOR_ACCOUNT),
            eq(LegacyUpdateMinorCreditorAccountResponse.class),
            any(),
            eq(null)
        )).thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Boom", null, null, null));

        mockMvc.perform(
                patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", AUTH_HEADER)
                    .header("If-Match", "\"1\"")
                    .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                    .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_missingPayload_returns400() throws Exception {
        super.patchMinorCreditor_missingPayload_returns400();
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

    private Object defaultGatewayResponse(InvocationOnMock invocation) {
        String actionType = invocation.getArgument(0);
        Object request = invocation.getArgument(2);

        if ("LIBRA.search_minor_creditors".equals(actionType)) {
            LegacyMinorCreditorSearchResultsRequest legacyRequest = (LegacyMinorCreditorSearchResultsRequest) request;
            if ("FAIL".equals(legacyRequest.getAccountNumber())) {
                throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Boom", null, null, null);
            }

            return new GatewayService.Response<>(
                HttpStatus.OK,
                LegacyMinorCreditorSearchResultsResponse.builder()
                    .count(2)
                    .creditorAccounts(java.util.List.of(
                        CreditorAccount.builder()
                            .creditorAccountId("104")
                            .accountNumber("12345678A")
                            .organisation(false)
                            .organisationName("Acme Supplies Ltd")
                            .addressLine1("Acme House")
                            .postcode("MA4 1AL")
                            .businessUnitName("Derbyshire")
                            .businessUnitId("10")
                            .accountBalance(150.0)
                            .defendant(Defendant.builder().organisation(false).build())
                            .build(),
                        CreditorAccount.builder()
                            .creditorAccountId("105")
                            .accountNumber("12345678")
                            .organisation(false)
                            .organisationName("Acme Supplies Ltd")
                            .addressLine1("Acme House")
                            .postcode("MA4 1AL")
                            .businessUnitName("Derbyshire")
                            .businessUnitId("10")
                            .accountBalance(0.0)
                            .defendant(Defendant.builder().organisation(false).build())
                            .build()
                    ))
                    .build(),
                null,
                null
            );
        }

        if ("LIBRA.get_minor_creditors_account_at_a_glance".equals(actionType)) {
            LegacyGetMinorCreditorAccountAtAGlanceRequest legacyRequest =
                (LegacyGetMinorCreditorAccountAtAGlanceRequest) request;
            if ("500".equals(legacyRequest.getCreditorAccountId())) {
                throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Boom", null, null, null);
            }

            return new GatewayService.Response<>(
                HttpStatus.OK,
                LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                    .party(LegacyPartyDetails.builder()
                        .partyId("99000000000901")
                        .organisationFlag(true)
                        .organisationDetails(OrganisationDetails.builder()
                            .organisationName("Speed Camera Services Ltd")
                            .build())
                        .build())
                    .address(AddressDetailsLegacy.builder()
                        .addressLine1("10 Technology Way")
                        .addressLine2("Reading")
                        .postcode("RG6 1PT")
                        .build())
                    .creditorAccountId(99000000000801L)
                    .creditorAccountVersion(BigInteger.ONE)
                    .defendant(LegacyGetMinorCreditorAccountAtAGlanceResponse.AtAGlanceDefendant.builder()
                        .accountNumber("12345678")
                        .accountId(99000000000001L)
                        .title("Mr")
                        .forenames("Michael James")
                        .surname("Johnson")
                        .build())
                    .payment(uk.gov.hmcts.opal.dto.legacy.common.LegacyPayment.builder()
                        .isBacs(true)
                        .holdPayment(false)
                        .build())
                    .build(),
                null,
                null
            );
        }

        if ("LIBRA.get_minor_creditors_account_header_summary".equals(actionType)) {
            String creditorAccountId = ((GetMinorCreditorAccountHeaderSummaryLegacyRequest) request)
                .getCreditorAccountId();
            if ("500".equals(creditorAccountId)) {
                throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Boom", null, null, null);
            }

            return new GatewayService.Response<>(
                HttpStatus.OK,
                GetMinorCreditorAccountHeaderSummaryLegacyResponse.builder()
                    .partyDetails(PartyDetailsLegacy.builder()
                        .partyId("99000000000900")
                        .organisationFlag(true)
                        .organisationDetails(OrganisationDetailsLegacy.builder()
                            .organisationName("Minor Creditor Test Ltd")
                            .build())
                        .build())
                    .businessUnit(BusinessUnitSummary.builder()
                        .businessUnitId("77")
                        .businessUnitName("Camberwell Green")
                        .welshSpeaking("N")
                        .build())
                    .creditor(GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy.builder()
                        .accountVersion(1)
                        .accountId(creditorAccountId)
                        .accountNumber("87654321")
                        .accountType(CreditorAccountTypeReference.builder().accountType("MN").build())
                        .hasAssociatedDefendant(false)
                        .build())
                    .financials(GetMinorCreditorAccountHeaderSummaryLegacyResponse.FinancialsLegacy.builder()
                        .awarded(BigDecimal.ZERO)
                        .paidOut(BigDecimal.ZERO)
                        .awaitingPayout(BigDecimal.ZERO)
                        .outstanding(BigDecimal.ZERO)
                        .build())
                    .build(),
                null,
                null
            );
        }

        if ("GET_MINOR_CREDITOR_ACCOUNT_PARTY".equals(actionType)) {
            String accountId = ((LegacyGetMinorCreditorAccountRequest) request).getAccountId();
            if ("500".equals(accountId)) {
                throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Boom", null, null, null);
            }

            return new GatewayService.Response<>(
                HttpStatus.OK,
                LegacyGetMinorCreditorAccountResponse.builder()
                    .accountVersion(1L)
                    .creditorAccountId(Long.valueOf(accountId))
                    .partyDetails(LegacyPartyDetails.builder()
                        .partyId("99000000000901")
                        .organisationFlag(true)
                        .organisationDetails(OrganisationDetails.builder()
                            .organisationName("Speed Camera Services Ltd")
                            .build())
                        .build())
                    .address(AddressDetailsLegacy.builder()
                        .addressLine1("10 Technology Way")
                        .addressLine2("Reading")
                        .postcode("RG6 1PT")
                        .build())
                    .payment(LegacyCreditorAccountPaymentDetails.builder()
                        .accountName("Speed Camera Services")
                        .sortCode("123456")
                        .accountNumber("12345678")
                        .accountReference("SCREF001")
                        .payByBacs(true)
                        .holdPayment(false)
                        .build())
                    .build(),
                null,
                null
            );
        }

        if (UPDATE_MINOR_CREDITOR_ACCOUNT.equals(actionType)) {
            return new GatewayService.Response<>(HttpStatus.OK, legacyPatchResponse(), null, null);
        }

        return null;
    }
}
