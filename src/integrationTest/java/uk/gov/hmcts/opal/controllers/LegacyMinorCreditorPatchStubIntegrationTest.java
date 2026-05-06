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

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.LegacyMinorCreditorPatchStubIntegrationTest")
class LegacyMinorCreditorPatchStubIntegrationTest extends MinorCreditorControllerIntegrationTest {

    private static final String AUTH_HEADER = "Bearer some_value";
    private static final String URL_BASE = "/minor-creditor-accounts";
    private static final long PATCH_MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID = 10;

    @Test
    void patchMinorCreditor_success_returns201_viaRealLegacyStub() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", "\"1\"")
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
        );

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
