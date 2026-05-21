package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionsToken;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionsToken;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;

@ActiveProfiles({"integration", "opal"})
@Slf4j(topic = "opal.MinorCreditorAuthIntegrationTest")
class MinorCreditorAuthIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/minor-creditor-accounts";
    private static final String AUTH_HEADER = "Bearer some_value";
    private static final long ACCOUNT_ID = 99000000000801L;
    private static final short BUSINESS_UNIT_ID = 77;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @Test
    void getMinorCreditorAccount_withBacsPermissionInSecurityContext_returnsBacsFields() throws Exception {
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.empty());

        mockMvc.perform(get(URL_BASE + "/{id}", ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", AUTH_HEADER)
                .with(authentication(permissionsToken(BUSINESS_UNIT_ID,
                    FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
                    FinesPermission.VIEW_CREDITOR_BACS))))
            .andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.creditor_account_id").value(ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.party_id").value("99000000000901"))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.payment.sort_code").value("123456"))
            .andExpect(jsonPath("$.payment.account_number").value("12345678"))
            .andExpect(jsonPath("$.payment.account_name").value("Speed Camera"))
            .andExpect(jsonPath("$.payment.account_reference").value("REF001"))
            .andExpect(jsonPath("$.payment.hold_payment").value(false));
    }

    @Test
    void getMinorCreditorAccount_withoutBacsPermissionInSecurityContext_redactsBacsFields() throws Exception {
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.empty());

        mockMvc.perform(get(URL_BASE + "/{id}", ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", AUTH_HEADER)
                .with(authentication(permissionsToken(BUSINESS_UNIT_ID,
                    FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))))
            .andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.creditor_account_id").value(ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.party_id").value("99000000000901"))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(nullValue()))
            .andExpect(jsonPath("$.payment.sort_code").value(nullValue()))
            .andExpect(jsonPath("$.payment.account_number").value(nullValue()))
            .andExpect(jsonPath("$.payment.account_name").value(nullValue()))
            .andExpect(jsonPath("$.payment.account_reference").value(nullValue()))
            .andExpect(jsonPath("$.payment.hold_payment").value(false));
    }

    @Test
    void getMinorCreditorAccount_withBacsPermissionInAuthenticatedUserState_returnsBacsFields() throws Exception {
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(permissionUser(
            BUSINESS_UNIT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
            FinesPermission.VIEW_CREDITOR_BACS
        )));

        mockMvc.perform(get(URL_BASE + "/{id}", ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", AUTH_HEADER)
                .with(authentication(noFinesPermissionsToken())))
            .andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.creditor_account_id").value(ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.party_id").value("99000000000901"))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.payment.sort_code").value("123456"))
            .andExpect(jsonPath("$.payment.account_number").value("12345678"))
            .andExpect(jsonPath("$.payment.account_name").value("Speed Camera"))
            .andExpect(jsonPath("$.payment.account_reference").value("REF001"))
            .andExpect(jsonPath("$.payment.hold_payment").value(false));
    }

    @Test
    void getMinorCreditorAccount_withoutBacsPermissionInAuthenticatedUserState_redactsBacsFields() throws Exception {
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(permissionUser(
            BUSINESS_UNIT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS
        )));

        mockMvc.perform(get(URL_BASE + "/{id}", ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", AUTH_HEADER)
                .with(authentication(permissionsToken(BUSINESS_UNIT_ID,
                    FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
                    FinesPermission.VIEW_CREDITOR_BACS))))
            .andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.creditor_account_id").value(ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.party_id").value("99000000000901"))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(nullValue()))
            .andExpect(jsonPath("$.payment.sort_code").value(nullValue()))
            .andExpect(jsonPath("$.payment.account_number").value(nullValue()))
            .andExpect(jsonPath("$.payment.account_name").value(nullValue()))
            .andExpect(jsonPath("$.payment.account_reference").value(nullValue()))
            .andExpect(jsonPath("$.payment.hold_payment").value(false));
    }
}
