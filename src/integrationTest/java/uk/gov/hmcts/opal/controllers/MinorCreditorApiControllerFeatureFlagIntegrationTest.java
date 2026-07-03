package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.MinorCreditorApiControllerFeatureFlagIntegrationTest")
class MinorCreditorApiControllerFeatureFlagIntegrationTest extends AbstractIntegrationTest {

    private static final long MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short BUSINESS_UNIT_ID = 10;

    @MockitoBean
    private MinorCreditorService minorCreditorService;

    @Autowired
    private CreditorAccountRepository creditorAccountRepository;

    @Test
    @JiraStory("PO-1992")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5977")
    void patchMinorCreditorAccount_whenLocalDefaultDisabled_returns404() throws Exception {
        PatchMinorCreditorAccountRequest request = patchMinorCreditorAccountRequest();

        ResultActions result = mockMvc.perform(patch("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("authorization", userStateStub.getBearerToken())
                            .header("Business-Unit-Id", String.valueOf(BUSINESS_UNIT_ID))
                            .header("If-Match", "\"1\"")
                            .content(objectMapper.writeValueAsString(request)));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditorAccount_whenLocalDefaultDisabled_returns404 body:\n{}",
            ToJsonString.toPrettyJson(body));
        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));

        CreditorAccountEntity creditorAccount = getCurrentCreditorAccount();
        assertFalse(creditorAccount.isHoldPayout());
        assertEquals(1L, creditorAccount.getVersionNumber());
        verifyNoInteractions(minorCreditorService);
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-8672")
    void getMinorCreditorHistory_whenRelease1bDisabled_returns404AndDoesNotCallService() throws Exception {
        ResultActions result = mockMvc.perform(get("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID + "/history")
                                                   .header("Authorization", "Bearer some_value"));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getMinorCreditorHistory_whenRelease1bDisabled_returns404AndDoesNotCallService body:\n{}",
            ToJsonString.toPrettyJson(body));
        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));

        verifyNoInteractions(minorCreditorService);
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
