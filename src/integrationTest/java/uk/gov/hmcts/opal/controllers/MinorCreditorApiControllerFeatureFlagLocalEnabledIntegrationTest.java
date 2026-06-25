package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.MinorCreditorApiControllerFeatureFlagLocalEnabledIntegrationTest")
class MinorCreditorApiControllerFeatureFlagLocalEnabledIntegrationTest
    extends AbstractIntegrationTest {

    private static final long MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short BUSINESS_UNIT_ID = 10;

    @Autowired
    private CreditorAccountRepository creditorAccountRepository;

    @Test
    @JiraStory("PO-1992")
    @JiraEpic("PO-2234")
    @JiraTestKey("PO-5978")
    void patchMinorCreditorAccount_whenLocalDefaultEnabled_returns200() throws Exception {
        PatchMinorCreditorAccountRequest request = patchMinorCreditorAccountRequest();

        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID,
            FinesPermission.ACCOUNT_MAINTENANCE,
            FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
            FinesPermission.VIEW_CREDITOR_BACS
        );

        ResultActions result = mockMvc.perform(patch("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .contentType(MediaType.APPLICATION_JSON)
            .header("authorization", userStateStub.getBearerToken())
            .header("Business-Unit-Id", String.valueOf(BUSINESS_UNIT_ID))
            .header("If-Match", "\"1\"")
            .content(objectMapper.writeValueAsString(request)));

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditorAccount_whenLocalDefaultEnabled_returns200 body:\n{}",
            ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.creditor_account_id").value(MINOR_CREDITOR_ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.party_id").value("99008"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Updated"))
            .andExpect(jsonPath("$.address.postcode").value("NW1 1AA"))
            .andExpect(jsonPath("$.payment.hold_payment").value(true));

        CreditorAccountEntity creditorAccount = getCurrentCreditorAccount();
        assertTrue(creditorAccount.isHoldPayout());
        assertEquals(3L, creditorAccount.getVersionNumber());
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
