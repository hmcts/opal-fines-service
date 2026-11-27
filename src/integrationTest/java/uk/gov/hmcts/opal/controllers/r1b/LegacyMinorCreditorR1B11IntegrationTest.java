package uk.gov.hmcts.opal.controllers.r1b;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Tag("ExtendedTest")
@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b-1-1=true"
})
@DisplayName("Legacy Minor Creditor History R1B11 Integration Test")
@Slf4j(topic = "opal.LegacyMinorCreditorHistoryR1B11IntegrationTest")
public class LegacyMinorCreditorR1B11IntegrationTest extends AbstractIntegrationTest {

    private static final long LEGACY_ACCOUNT_ID = 99000000000802L;

    @Test
    @JiraEpic("PO-11478")
    @JiraStory("PO-11622")
    @JiraStory("PO-11632")
    @DisplayName("LEGACY: resolves Legacy BusinessUnit ID when R1B11 flag is enabled")
    void getMinorCreditorAccount_whenR1B11IsEnabled_resolvesLegacyBusinessUnitId() throws Exception {
        ResultActions result = mockMvc.perform(get("/minor-creditor-accounts/{id}", LEGACY_ACCOUNT_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"9223372036854775808\""))
            .andExpect(jsonPath("$.creditor_account_id").value(LEGACY_ACCOUNT_ID))
            .andExpect(jsonPath("$.business_unit_id").doesNotExist())
            .andExpect(jsonPath("$.address").exists())
            .andExpect(jsonPath("$.party_details.party_id").value(99000000000901L))
            .andExpect(jsonPath("$.payment.account_name").value("Speed Camera Services"))
            .andExpect(jsonPath("$.payment.account_number").value("12345678"))
            .andExpect(jsonPath("$.payment.account_reference").value("SCREF001"))
            .andExpect(jsonPath("$.payment.hold_payment").value(false))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.payment.sort_code").value("123456"))
            .andExpect(jsonPath("$.repayment").value(false));
    }

    @Test
    @JiraEpic("PO-11478")
    @JiraStory("PO-11622")
    @JiraStory("PO-11632")
    @DisplayName("LEGACY: redacts bank details when the user lacks VIEW_CREDITOR_BACS")
    void getMinorCreditorAccount_withoutViewCreditorBacs_redactsBankDetails() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        ResultActions result = mockMvc.perform(get("/minor-creditor-accounts/{id}", LEGACY_ACCOUNT_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"9223372036854775808\""))
            .andExpect(jsonPath("$.creditor_account_id").value(LEGACY_ACCOUNT_ID))
            .andExpect(jsonPath("$.business_unit_id").doesNotExist())
            .andExpect(jsonPath("$.address").exists())
            .andExpect(jsonPath("$.party_details.party_id").value(99000000000901L))
            .andExpect(jsonPath("$.payment.account_name").doesNotExist())
            .andExpect(jsonPath("$.payment.account_number").doesNotExist())
            .andExpect(jsonPath("$.payment.account_reference").doesNotExist())
            .andExpect(jsonPath("$.payment.hold_payment").value(false))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.payment.sort_code").doesNotExist())
            .andExpect(jsonPath("$.repayment").value(false));
    }

    @Test
    @JiraEpic("PO-11478")
    @JiraStory("PO-11622")
    @JiraStory("PO-11632")
    @DisplayName("LEGACY: redacts bank details when VIEW_CREDITOR_BACS is granted only for another business unit")
    void getMinorCreditorAccount_withBacsPermissionOnlyForAnotherBusinessUnit_redactsBankDetails() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 99, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
            FinesPermission.VIEW_CREDITOR_BACS);

        ResultActions result = mockMvc.perform(get("/minor-creditor-accounts/{id}", LEGACY_ACCOUNT_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"9223372036854775808\""))
            .andExpect(jsonPath("$.creditor_account_id").value(LEGACY_ACCOUNT_ID))
            .andExpect(jsonPath("$.business_unit_id").doesNotExist())
            .andExpect(jsonPath("$.address").exists())
            .andExpect(jsonPath("$.party_details.party_id").value(99000000000901L))
            .andExpect(jsonPath("$.payment.account_name").doesNotExist())
            .andExpect(jsonPath("$.payment.account_number").doesNotExist())
            .andExpect(jsonPath("$.payment.account_reference").doesNotExist())
            .andExpect(jsonPath("$.payment.hold_payment").value(false))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.payment.sort_code").doesNotExist())
            .andExpect(jsonPath("$.repayment").value(false));
    }
}
