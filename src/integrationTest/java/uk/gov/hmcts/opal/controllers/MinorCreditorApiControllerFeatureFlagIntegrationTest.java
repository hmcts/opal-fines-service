package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.AbstractMinorCreditorApiControllerFeatureFlagIntegrationTest")
abstract class AbstractMinorCreditorApiControllerFeatureFlagIntegrationTest extends AbstractIntegrationTest {

    private static final String AUTH_HEADER = "Bearer some_value";
    private static final long MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short BUSINESS_UNIT_ID = 10;

    @MockitoBean
    private UserStateService userStateService;

    @Autowired
    private CreditorAccountRepository creditorAccountRepository;

    protected void patchMinorCreditorAccount_whenLocalDefaultEnabled_returns201() throws Exception {
        PatchMinorCreditorAccountRequest request = patchMinorCreditorAccountRequest();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(permissionUser(
            BUSINESS_UNIT_ID,
            FinesPermission.ACCOUNT_MAINTENANCE,
            FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD
        ));

        ResultActions result = mockMvc.perform(patch("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("Business-Unit-Id", String.valueOf(BUSINESS_UNIT_ID))
                            .header("If-Match", "\"1\"")
                            .content(objectMapper.writeValueAsString(request)));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditorAccount_whenLocalDefaultEnabled_returns201 body:\n{}",
            ToJsonString.toPrettyJson(body));

        result.andExpect(status().isCreated())
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.creditor_account_id").value(MINOR_CREDITOR_ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.party_id").value("99008"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Updated"))
            .andExpect(jsonPath("$.address.postcode").value("NW1 1AA"))
            .andExpect(jsonPath("$.payment.hold_payment").value(true));

        CreditorAccountEntity creditorAccount = getCurrentCreditorAccount();
        assertTrue(creditorAccount.isHoldPayout());
        assertEquals(2L, creditorAccount.getVersionNumber());
    }

    protected void patchMinorCreditorAccount_whenLocalDefaultDisabled_returns405() throws Exception {
        PatchMinorCreditorAccountRequest request = patchMinorCreditorAccountRequest();

        ResultActions result = mockMvc.perform(patch("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("Business-Unit-Id", String.valueOf(BUSINESS_UNIT_ID))
                            .header("If-Match", "\"1\"")
                            .content(objectMapper.writeValueAsString(request)));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditorAccount_whenLocalDefaultDisabled_returns405 body:\n{}",
            ToJsonString.toPrettyJson(body));

        result.andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));

        CreditorAccountEntity creditorAccount = getCurrentCreditorAccount();
        assertFalse(creditorAccount.isHoldPayout());
        assertEquals(1L, creditorAccount.getVersionNumber());
    }

    private PatchMinorCreditorAccountRequest patchMinorCreditorAccountRequest() {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                              .partyId("99008")
                              .organisationFlag(false)
                              .individualDetails(new IndividualDetailsCommon()
                                                     .forenames("Creditor")
                                                     .surname("Updated")))
            .address(new AddressDetailsCommon()
                         .addressLine1("99 Updated Road")
                         .postcode("NW1 1AA"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                         .holdPayment(true)
                         .payByBacs(true));
    }

    private CreditorAccountEntity getCurrentCreditorAccount() {
        return creditorAccountRepository.findById(MINOR_CREDITOR_ACCOUNT_ID).orElseThrow();
    }
}

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
class MinorCreditorApiControllerFeatureFlagIntegrationTest
    extends AbstractMinorCreditorApiControllerFeatureFlagIntegrationTest {

    @Test
    protected void patchMinorCreditorAccount_whenLocalDefaultDisabled_returns405() throws Exception {
        super.patchMinorCreditorAccount_whenLocalDefaultDisabled_returns405();
    }
}

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
class MinorCreditorApiControllerFeatureFlagLocalEnabledIntegrationTest
    extends AbstractMinorCreditorApiControllerFeatureFlagIntegrationTest {

    @Test
    protected void patchMinorCreditorAccount_whenLocalDefaultEnabled_returns201() throws Exception {
        super.patchMinorCreditorAccount_whenLocalDefaultEnabled_returns201();
    }
}
