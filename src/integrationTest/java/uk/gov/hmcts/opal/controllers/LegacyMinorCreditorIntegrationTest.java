package uk.gov.hmcts.opal.controllers;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
public class LegacyMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    private static final String AUTH_HEADER = "Bearer some_value";
    private static final String URL_BASE = "/minor-creditor-accounts";
    private static final long PATCH_MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID = 10;

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
    void patchMinorCreditor_withoutPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutPermission_returns403();
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
    void patchMinorCreditor_missingPayload_returns400() throws Exception {
        super.patchMinorCreditor_missingPayload_returns400();
    }

    @Test
    void patchMinorCreditor_success_returns201_viaRealLegacyStub() throws Exception {
        authorisePatchUser();

        ResultActions resultActions = performLegacyPatch(PATCH_MINOR_CREDITOR_ACCOUNT_ID, "\"1\"");

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditor_success_returns201_viaRealLegacyStub body:\n{}",
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
    }

    @Test
    void patchMinorCreditor_notFound_returns404_viaRealLegacyStub() throws Exception {
        authorisePatchUser();

        performLegacyPatch(404L, "\"1\"")
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_timeout_returns408_viaRealLegacyStub() throws Exception {
        authorisePatchUser();

        performLegacyPatch(408L, "\"1\"")
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_staleVersion_returns409_viaRealLegacyStub() throws Exception {
        authorisePatchUser();

        performLegacyPatch(409L, "\"2\"")
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_serviceUnavailable_returns503_viaRealLegacyStub() throws Exception {
        authorisePatchUser();

        performLegacyPatch(503L, "\"1\"")
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void patchMinorCreditor_serverError_returns500_viaRealLegacyStub() throws Exception {
        authorisePatchUser();

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
